package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.common.security.DataScope;
import com.smarthis.patient.converter.AdmissionConverter;
import com.smarthis.patient.dto.request.AdmissionCreateRequest;
import com.smarthis.patient.dto.request.AdmissionDepositRequest;
import com.smarthis.patient.dto.request.AdmissionDischargeRequest;
import com.smarthis.patient.dto.request.AdmissionQueryRequest;
import com.smarthis.patient.dto.request.AdmissionTransferRequest;
import com.smarthis.patient.dto.response.AdmissionVo;
import com.smarthis.patient.entity.Admission;
import com.smarthis.patient.entity.AdmissionTransfer;
import com.smarthis.patient.entity.Patient;
import com.smarthis.patient.mapper.AdmissionMapper;
import com.smarthis.patient.mapper.AdmissionTransferMapper;
import com.smarthis.patient.mapper.InpatientBedMapper;
import com.smarthis.patient.mapper.PatientMapper;
import com.smarthis.patient.service.AdmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionServiceImpl implements AdmissionService {

    private final AdmissionMapper admissionMapper;
    private final PatientMapper patientMapper;
    private final AdmissionTransferMapper admissionTransferMapper;
    private final InpatientBedMapper inpatientBedMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public AdmissionVo create(AdmissionCreateRequest request) {
        Patient patient = patientMapper.selectById(request.getPatientId());
        if (patient == null || patient.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        Admission admission = AdmissionConverter.toEntity(request);
        admission.setAdmissionNo(bizNoGenerator.next(BizNoType.ADMISSION));
        admissionMapper.insert(admission);

        log.info("Admission created: admissionNo={}, patientId={}, deptId={}", admission.getAdmissionNo(), request.getPatientId(), request.getDeptId());
        AdmissionVo vo = AdmissionConverter.toVo(admission);
        vo.setPatientName(patient.getName());
        return vo;
    }

    @Override
    public AdmissionVo getById(Long id) {
        Admission admission = getEntity(id);
        AdmissionVo vo = AdmissionConverter.toVo(admission);
        Patient patient = patientMapper.selectById(admission.getPatientId());
        if (patient != null) {
            vo.setPatientName(patient.getName());
        }
        return vo;
    }

    @Override
    @Transactional
    public void admit(Long id) {
        Admission admission = getEntity(id);
        if (!"PLANNED".equals(admission.getAdmissionStatus())) {
            throw new BusinessException(ErrorCode.ADMISSION_STATUS_INVALID);
        }
        admission.setAdmissionStatus("ADMITTED");
        admission.setAdmissionDate(LocalDate.now());
        admissionMapper.updateById(admission);
        occupyBed(admission.getBedId(), admission.getId());
        log.info("Admission admitted: id={}", id);
    }

    @Override
    @Transactional
    public void discharge(Long id, AdmissionDischargeRequest request) {
        Admission admission = getEntity(id);
        if (!"ADMITTED".equals(admission.getAdmissionStatus())) {
            throw new BusinessException(ErrorCode.ADMISSION_STATUS_INVALID);
        }
        admission.setAdmissionStatus("DISCHARGED");
        admission.setActualDischargeDate(request.getActualDischargeDate() != null ? request.getActualDischargeDate() : LocalDate.now());
        admission.setDischargeType(request.getDischargeType());
        admission.setDischargeSummary(request.getDischargeSummary());
        admissionMapper.updateById(admission);
        releaseBed(admission.getBedId(), admission.getId());
        log.info("Admission discharged: id={}", id);
    }

    @Override
    @Transactional
    public AdmissionVo transfer(Long id, AdmissionTransferRequest request) {
        Admission admission = getEntity(id);
        if (!"ADMITTED".equals(admission.getAdmissionStatus())) {
            throw new BusinessException(ErrorCode.ADMISSION_STATUS_INVALID);
        }
        if (request.getTargetBedId().equals(admission.getBedId())
                && request.getTargetWardId().equals(admission.getWardId())
                && request.getTargetDeptId().equals(admission.getDeptId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        occupyBed(request.getTargetBedId(), admission.getId());
        Long previousBedId = admission.getBedId();
        AdmissionTransfer transfer = new AdmissionTransfer();
        transfer.setAdmissionId(admission.getId());
        transfer.setFromDeptId(admission.getDeptId());
        transfer.setFromWardId(admission.getWardId());
        transfer.setFromBedId(admission.getBedId());
        transfer.setToDeptId(request.getTargetDeptId());
        transfer.setToWardId(request.getTargetWardId());
        transfer.setToBedId(request.getTargetBedId());
        transfer.setTransferReason(request.getReason());
        transfer.setTransferTime(LocalDateTime.now());
        admissionTransferMapper.insert(transfer);

        admission.setDeptId(request.getTargetDeptId());
        admission.setWardId(request.getTargetWardId());
        admission.setBedId(request.getTargetBedId());
        admissionMapper.updateById(admission);
        releaseBed(previousBedId, admission.getId());
        log.info("Admission transferred: id={}, deptId={}, wardId={}, bedId={}", id,
                request.getTargetDeptId(), request.getTargetWardId(), request.getTargetBedId());
        return getById(id);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Admission admission = getEntity(id);
        if ("DISCHARGED".equals(admission.getAdmissionStatus())) {
            throw new BusinessException(ErrorCode.ADMISSION_STATUS_INVALID);
        }
        admission.setAdmissionStatus("CANCELLED");
        admissionMapper.updateById(admission);
        log.info("Admission cancelled: id={}", id);
    }

    @Override
    @Transactional
    public void addDeposit(Long id, AdmissionDepositRequest request) {
        Admission admission = getEntity(id);
        if (!"ADMITTED".equals(admission.getAdmissionStatus()) && !"PLANNED".equals(admission.getAdmissionStatus())) {
            throw new BusinessException(ErrorCode.ADMISSION_STATUS_INVALID);
        }
        admission.setTotalDeposit(admission.getTotalDeposit().add(request.getAmount()));
        admissionMapper.updateById(admission);
        log.info("Deposit added: admissionId={}, amount={}, totalDeposit={}", id, request.getAmount(), admission.getTotalDeposit());
    }

    @Override
    public PageResult<AdmissionVo> list(AdmissionQueryRequest request) {
        LambdaQueryWrapper<Admission> query = new LambdaQueryWrapper<>();
        query.eq(Admission::getDeleted, 0);

        if (request.getPatientId() != null) {
            query.eq(Admission::getPatientId, request.getPatientId());
        }
        Long scopedDeptId = DataScope.restrictDepartment(request.getDeptId());
        if (scopedDeptId != null) {
            query.eq(Admission::getDeptId, scopedDeptId);
        }
        if (request.getWardId() != null) {
            query.eq(Admission::getWardId, request.getWardId());
        }
        if (request.getAdmissionStatus() != null) {
            query.eq(Admission::getAdmissionStatus, request.getAdmissionStatus());
        }
        if (request.getAdmissionType() != null) {
            query.eq(Admission::getAdmissionType, request.getAdmissionType());
        }
        if (request.getAdmissionDateFrom() != null) {
            query.ge(Admission::getAdmissionDate, request.getAdmissionDateFrom());
        }
        if (request.getAdmissionDateTo() != null) {
            query.le(Admission::getAdmissionDate, request.getAdmissionDateTo());
        }
        query.orderByDesc(Admission::getAdmissionDate);

        Page<Admission> page = new Page<>(request.toPage().getCurrent(), request.toPage().getSize());
        Page<Admission> result = admissionMapper.selectPage(page, query);

        List<Admission> admissions = result.getRecords();
        Map<Long, Patient> patientMap = loadPatients(admissions.stream()
                .map(Admission::getPatientId).distinct().toList());

        List<AdmissionVo> records = admissions.stream().map(a -> {
            AdmissionVo vo = AdmissionConverter.toVo(a);
            Patient patient = patientMap.get(a.getPatientId());
            if (patient != null) {
                vo.setPatientName(patient.getName());
            }
            return vo;
        }).toList();

        return new PageResult<>(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public List<AdmissionVo> listByPatient(Long patientId) {
        LambdaQueryWrapper<Admission> query = new LambdaQueryWrapper<>();
        query.eq(Admission::getPatientId, patientId)
                .eq(Admission::getDeleted, 0)
                .orderByDesc(Admission::getAdmissionDate);
        List<Admission> admissions = admissionMapper.selectList(query);
        Patient patient = patientMapper.selectById(patientId);
        String patientName = patient != null ? patient.getName() : null;
        return admissions.stream().map(vo -> {
            AdmissionVo admissionVo = AdmissionConverter.toVo(vo);
            admissionVo.setPatientName(patientName);
            return admissionVo;
        }).toList();
    }

    private Admission getEntity(Long id) {
        Admission admission = admissionMapper.selectById(id);
        if (admission == null || admission.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.ADMISSION_NOT_FOUND);
        }
        return admission;
    }

    private void occupyBed(Long bedId, Long admissionId) {
        if (bedId != null && inpatientBedMapper.occupy(bedId, admissionId) != 1) {
            throw new BusinessException(ErrorCode.BED_NOT_AVAILABLE);
        }
    }

    private void releaseBed(Long bedId, Long admissionId) {
        if (bedId != null && inpatientBedMapper.release(bedId, admissionId) != 1) {
            log.warn("Bed release mismatch: bedId={}, admissionId={}", bedId, admissionId);
        }
    }

    private Map<Long, Patient> loadPatients(List<Long> patientIds) {
        if (patientIds.isEmpty()) {
            return Map.of();
        }
        return patientMapper.selectBatchIds(patientIds).stream()
                .collect(Collectors.toMap(Patient::getId, Function.identity()));
    }
}
