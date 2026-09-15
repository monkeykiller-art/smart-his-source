package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.event.EventPublisher;
import com.smarthis.common.event.HisEvent;
import com.smarthis.common.event.KafkaTopics;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.patient.client.OperationsClient;
import com.smarthis.patient.converter.RegistrationConverter;
import com.smarthis.patient.dto.request.RegistrationCreateRequest;
import com.smarthis.patient.dto.request.RegistrationQueryRequest;
import com.smarthis.patient.dto.response.RegistrationVo;
import com.smarthis.patient.entity.Department;
import com.smarthis.patient.entity.Doctor;
import com.smarthis.patient.entity.Encounter;
import com.smarthis.patient.entity.Patient;
import com.smarthis.patient.entity.PatientIdentifier;
import com.smarthis.patient.entity.Registration;
import com.smarthis.patient.entity.Schedule;
import com.smarthis.patient.entity.Triage;
import com.smarthis.patient.mapper.DepartmentMapper;
import com.smarthis.patient.mapper.DoctorMapper;
import com.smarthis.patient.mapper.EncounterMapper;
import com.smarthis.patient.mapper.PatientIdentifierMapper;
import com.smarthis.patient.mapper.PatientMapper;
import com.smarthis.patient.mapper.RegistrationMapper;
import com.smarthis.patient.mapper.ScheduleMapper;
import com.smarthis.patient.mapper.TriageMapper;
import com.smarthis.patient.service.RegistrationService;
import com.smarthis.patient.support.EmpiMatcher;
import com.smarthis.patient.support.QuotaManager;
import com.smarthis.patient.support.VisitSeqAllocator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final PatientMapper patientMapper;
    private final PatientIdentifierMapper identifierMapper;
    private final ScheduleMapper scheduleMapper;
    private final EncounterMapper encounterMapper;
    private final TriageMapper triageMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final EmpiMatcher empiMatcher;
    private final QuotaManager quotaManager;
    private final VisitSeqAllocator visitSeqAllocator;
    private final BizNoGenerator bizNoGenerator;
    private final OperationsClient operationsClient;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public RegistrationVo create(RegistrationCreateRequest request) {
        // Step 1: resolve the patient — explicit id, EMPI match, or brand new record
        Patient patient = resolvePatient(request);

        // Step 2: validate the schedule and acquire one quota slot
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null || schedule.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        if (!"ACTIVE".equals(schedule.getScheduleStatus())) {
            throw new BusinessException(ErrorCode.SCHEDULE_NO_QUOTA);
        }
        LambdaQueryWrapper<Registration> duplicateQuery = new LambdaQueryWrapper<>();
        duplicateQuery.eq(Registration::getPatientId, patient.getId())
                .eq(Registration::getScheduleId, schedule.getId())
                .eq(Registration::getRegStatus, "ACTIVE")
                .eq(Registration::getDeleted, 0);
        if (registrationMapper.selectCount(duplicateQuery) > 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_DUPLICATE);
        }
        quotaManager.acquire(schedule.getId());

        // Step 3: generate the registration business number
        String regNo = bizNoGenerator.next(BizNoType.REGISTRATION);

        // Step 4: allocate the visit sequence within the schedule
        int visitSeq = visitSeqAllocator.allocate(schedule.getId());

        // Step 5: persist the registration
        Registration reg = new Registration();
        reg.setRegNo(regNo);
        reg.setPatientId(patient.getId());
        reg.setScheduleId(schedule.getId());
        reg.setDeptId(schedule.getDeptId());
        reg.setDoctorId(schedule.getDoctorId());
        reg.setVisitSeq(visitSeq);
        reg.setRegDate(schedule.getScheduleDate());
        reg.setTimePeriod(schedule.getTimePeriod());
        reg.setRegFee(schedule.getRegFee());
        reg.setPayStatus("UNPAID");
        reg.setRegSource(request.getRegSource() != null ? request.getRegSource() : "WINDOW");
        reg.setRegStatus("ACTIVE");
        registrationMapper.insert(reg);

        // Step 6: open the planned encounter
        Encounter encounter = new Encounter();
        encounter.setEncounterNo(bizNoGenerator.next(BizNoType.REGISTRATION).replace("MZ", "JZ"));
        encounter.setPatientId(patient.getId());
        encounter.setRegId(reg.getId());
        encounter.setDeptId(schedule.getDeptId());
        encounter.setDoctorId(schedule.getDoctorId());
        encounter.setEncounterType("OUTPATIENT");
        encounter.setEncounterStatus("PLANNED");
        encounter.setVisitDate(LocalDate.now());
        encounterMapper.insert(encounter);

        // Step 7: ask his-operations for the registration bill (best effort, compensated later)
        createBill(patient, schedule, reg, encounter.getId());

        // Step 8: enqueue the patient for triage
        Triage triage = new Triage();
        triage.setRegId(reg.getId());
        triage.setPatientId(patient.getId());
        triage.setDeptId(schedule.getDeptId());
        triage.setDoctorId(schedule.getDoctorId());
        triage.setVisitSeq(visitSeq);
        triage.setTriageStatus("QUEUED");
        triage.setQueueNo(visitSeq);
        triage.setEnqueueTime(LocalDateTime.now());
        triageMapper.insert(triage);

        // Step 9: publish the domain event only once the transaction has committed
        publishRegisteredAfterCommit(patient, schedule, reg, encounter);

        // Step 10: assemble the response
        RegistrationVo vo = enrichVo(reg);
        vo.setEncounterId(encounter.getId());
        log.info("Registration created: regNo={}, patientId={}, scheduleId={}, visitSeq={}",
                regNo, patient.getId(), schedule.getId(), visitSeq);
        return vo;
    }

    @Override
    public RegistrationVo getById(Long id) {
        Registration reg = registrationMapper.selectById(id);
        if (reg == null || reg.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }
        return enrichVo(reg);
    }

    @Override
    @Transactional
    public void cancel(Long id, String reason) {
        Registration reg = registrationMapper.selectById(id);
        if (reg == null || reg.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }
        if ("CANCELLED".equals(reg.getRegStatus())) {
            throw new BusinessException(ErrorCode.REGISTRATION_CANCELLED);
        }
        if ("PAID".equals(reg.getPayStatus())) {
            throw new BusinessException(ErrorCode.REGISTRATION_PAYMENT_REQUIRED);
        }
        reg.setRegStatus("CANCELLED");
        reg.setCancelReason(reason);
        reg.setCancelTime(LocalDateTime.now());
        registrationMapper.updateById(reg);

        quotaManager.release(reg.getScheduleId());
        log.info("Registration cancelled: id={}, reason={}", id, reason);
    }

    @Override
    @Transactional
    public void markPaid(Long id, Long billId) {
        Registration reg = registrationMapper.selectById(id);
        if (reg == null || reg.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }
        if ("CANCELLED".equals(reg.getRegStatus())) {
            throw new BusinessException(ErrorCode.REGISTRATION_CANCELLED);
        }
        if ("PAID".equals(reg.getPayStatus())) {
            throw new BusinessException(ErrorCode.REGISTRATION_ALREADY_PAID);
        }
        reg.setPayStatus("PAID");
        reg.setPayTime(LocalDateTime.now());
        if (billId != null) {
            reg.setBillId(billId);
        }
        registrationMapper.updateById(reg);
        log.info("Registration marked paid: id={}, billId={}", id, reg.getBillId());
    }

    @Override
    @Transactional
    public void refund(Long id, String reason) {
        Registration reg = registrationMapper.selectById(id);
        if (reg == null || reg.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }
        if ("CANCELLED".equals(reg.getRegStatus())) {
            throw new BusinessException(ErrorCode.REGISTRATION_CANCELLED);
        }
        if (!"PAID".equals(reg.getPayStatus())) {
            throw new BusinessException(ErrorCode.REGISTRATION_PAYMENT_REQUIRED);
        }
        reg.setPayStatus("REFUNDED");
        reg.setRegStatus("CANCELLED");
        reg.setCancelReason(reason);
        reg.setCancelTime(LocalDateTime.now());
        registrationMapper.updateById(reg);

        quotaManager.release(reg.getScheduleId());
        log.info("Registration refunded: id={}, reason={}", id, reason);
    }

    @Override
    public PageResult<RegistrationVo> query(RegistrationQueryRequest request) {
        LambdaQueryWrapper<Registration> query = new LambdaQueryWrapper<>();
        query.eq(Registration::getDeleted, 0);
        if (request.getPatientId() != null) {
            query.eq(Registration::getPatientId, request.getPatientId());
        }
        if (request.getDoctorId() != null) {
            query.eq(Registration::getDoctorId, request.getDoctorId());
        }
        if (request.getDeptId() != null) {
            query.eq(Registration::getDeptId, request.getDeptId());
        }
        if (request.getRegDate() != null) {
            query.eq(Registration::getRegDate, request.getRegDate());
        }
        if (StringUtils.hasText(request.getPayStatus())) {
            query.eq(Registration::getPayStatus, request.getPayStatus());
        }
        if (StringUtils.hasText(request.getRegStatus())) {
            query.eq(Registration::getRegStatus, request.getRegStatus());
        }
        query.orderByDesc(Registration::getCreatedTime);

        Page<Registration> page = request.toPage();
        IPage<Registration> result = registrationMapper.selectPage(page, query);
        List<RegistrationVo> vos = result.getRecords().stream()
                .map(this::enrichVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    private Patient resolvePatient(RegistrationCreateRequest request) {
        if (request.getPatientId() != null) {
            Patient existing = patientMapper.selectById(request.getPatientId());
            if (existing == null || existing.getDeleted() != 0) {
                throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
            }
            return existing;
        }

        EmpiMatcher.MatchResult matchResult = empiMatcher.match(
                request.getIdType(), request.getIdNo(),
                request.getName(), request.getPhone(),
                request.getGender(), request.getBirthDate());

        if (!matchResult.isNew) {
            // A 60-79 score is an ambiguous candidate; require manual disambiguation
            if (matchResult.score >= 60 && matchResult.score < 80) {
                throw new BusinessException(ErrorCode.PATIENT_DUPLICATE);
            }
            return matchResult.patient;
        }

        Patient patient = new Patient();
        patient.setEmpiNo(bizNoGenerator.next(BizNoType.REGISTRATION).replace("MZ", "EM"));
        patient.setName(request.getName());
        patient.setGender(request.getGender());
        patient.setBirthDate(request.getBirthDate());
        patient.setIdType(request.getIdType());
        patient.setIdNo(request.getIdNo());
        patient.setPhone(request.getPhone());
        patient.setPatientType("NORMAL");
        patient.setPatientStatus("ACTIVE");
        patient.setSource(request.getRegSource() != null ? request.getRegSource() : "SELF");
        patientMapper.insert(patient);

        if (StringUtils.hasText(request.getIdType()) && StringUtils.hasText(request.getIdNo())) {
            PatientIdentifier identifier = new PatientIdentifier();
            identifier.setPatientId(patient.getId());
            identifier.setIdType(request.getIdType());
            identifier.setIdNo(request.getIdNo());
            identifier.setIsPrimary(1);
            identifierMapper.insert(identifier);
        }
        log.info("New patient created during registration: id={}, empiNo={}", patient.getId(), patient.getEmpiNo());
        return patient;
    }

    private void createBill(Patient patient, Schedule schedule, Registration reg, Long encounterId) {
        try {
            Map<String, Object> billReq = new HashMap<>();
            billReq.put("patientId", patient.getId());
            billReq.put("patientName", patient.getName());
            billReq.put("regId", reg.getId());
            billReq.put("regNo", reg.getRegNo());
            billReq.put("encounterId", encounterId);
            billReq.put("visitType", "OUTPATIENT");
            billReq.put("deptId", schedule.getDeptId());
            billReq.put("doctorId", schedule.getDoctorId());
            billReq.put("amount", schedule.getRegFee());
            billReq.put("sourceType", "REGISTRATION");
            ApiResponse<Map<String, Object>> billResp = operationsClient.createRegistrationBill(billReq);
            if (billResp != null && billResp.getCode() == 200 && billResp.getData() != null) {
                Object billId = billResp.getData().get("id");
                if (billId != null) {
                    reg.setBillId(Long.valueOf(billId.toString()));
                    registrationMapper.updateById(reg);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to create registration bill via Feign for regNo={}, will be compensated: {}",
                    reg.getRegNo(), e.getMessage());
        }
    }

    private void publishRegisteredAfterCommit(Patient patient, Schedule schedule,
                                              Registration reg, Encounter encounter) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            doPublishRegistered(patient, schedule, reg, encounter);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                doPublishRegistered(patient, schedule, reg, encounter);
            }
        });
    }

    private void doPublishRegistered(Patient patient, Schedule schedule,
                                     Registration reg, Encounter encounter) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("regId", reg.getId());
        payload.put("regNo", reg.getRegNo());
        payload.put("patientId", patient.getId());
        payload.put("patientName", patient.getName());
        payload.put("deptId", schedule.getDeptId());
        payload.put("doctorId", schedule.getDoctorId());
        payload.put("encounterId", encounter.getId());
        eventPublisher.publish(KafkaTopics.PATIENT_REGISTERED,
                HisEvent.of("patient.registered", "his-patient", payload));
    }

    private RegistrationVo enrichVo(Registration reg) {
        RegistrationVo vo = RegistrationConverter.toVo(reg);
        if (reg.getPatientId() != null) {
            Patient patient = patientMapper.selectById(reg.getPatientId());
            if (patient != null) {
                vo.setPatientName(patient.getName());
            }
        }
        if (reg.getDoctorId() != null) {
            Doctor doc = doctorMapper.selectById(reg.getDoctorId());
            if (doc != null) {
                vo.setDoctorName(doc.getDoctorName());
            }
        }
        if (reg.getDeptId() != null) {
            Department dept = departmentMapper.selectById(reg.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        return vo;
    }
}
