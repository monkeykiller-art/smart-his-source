package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.patient.dto.response.TriageVo;
import com.smarthis.patient.entity.Department;
import com.smarthis.patient.entity.Doctor;
import com.smarthis.patient.entity.Encounter;
import com.smarthis.patient.entity.Patient;
import com.smarthis.patient.entity.Registration;
import com.smarthis.patient.entity.Triage;
import com.smarthis.patient.mapper.DepartmentMapper;
import com.smarthis.patient.mapper.DoctorMapper;
import com.smarthis.patient.mapper.EncounterMapper;
import com.smarthis.patient.mapper.PatientMapper;
import com.smarthis.patient.mapper.RegistrationMapper;
import com.smarthis.patient.mapper.TriageMapper;
import com.smarthis.patient.service.TriageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriageServiceImpl implements TriageService {

    private final TriageMapper triageMapper;
    private final RegistrationMapper registrationMapper;
    private final EncounterMapper encounterMapper;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public TriageVo enqueue(Long regId) {
        Registration reg = registrationMapper.selectById(regId);
        if (reg == null || reg.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }
        if ("CANCELLED".equals(reg.getRegStatus())) {
            throw new BusinessException(ErrorCode.REGISTRATION_CANCELLED);
        }

        LambdaQueryWrapper<Triage> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(Triage::getRegId, regId).eq(Triage::getDeleted, 0);
        Triage existing = firstOrNull(existQuery);
        if (existing != null) {
            return toVo(existing);
        }

        Triage triage = new Triage();
        triage.setRegId(regId);
        triage.setPatientId(reg.getPatientId());
        triage.setDeptId(reg.getDeptId());
        triage.setDoctorId(reg.getDoctorId());
        triage.setVisitSeq(reg.getVisitSeq());
        triage.setTriageStatus("QUEUED");
        triage.setQueueNo(reg.getVisitSeq());
        triage.setEnqueueTime(LocalDateTime.now());
        triageMapper.insert(triage);
        log.info("Triage enqueued: regId={}, queueNo={}", regId, triage.getQueueNo());
        return toVo(triage);
    }

    @Override
    @Transactional
    public TriageVo callNext(Long deptId, Long doctorId) {
        LambdaQueryWrapper<Triage> query = new LambdaQueryWrapper<>();
        query.eq(Triage::getDeptId, deptId)
                .eq(Triage::getTriageStatus, "QUEUED")
                .eq(Triage::getDeleted, 0);
        if (doctorId != null) {
            query.eq(Triage::getDoctorId, doctorId);
        }
        query.orderByAsc(Triage::getQueueNo);
        Triage triage = firstOrNull(query);
        if (triage == null) {
            throw new BusinessException(ErrorCode.TRIAGE_NOT_FOUND);
        }
        triage.setTriageStatus("CALLED");
        triage.setCallTime(LocalDateTime.now());
        triageMapper.updateById(triage);

        // Move the linked encounter into progress so the doctor can start documenting
        if (triage.getRegId() != null) {
            LambdaQueryWrapper<Encounter> encQuery = new LambdaQueryWrapper<>();
            encQuery.eq(Encounter::getRegId, triage.getRegId()).eq(Encounter::getDeleted, 0);
            List<Encounter> encounters = encounterMapper.selectList(encQuery);
            if (!encounters.isEmpty()) {
                Encounter encounter = encounters.get(0);
                if ("PLANNED".equals(encounter.getEncounterStatus())) {
                    encounter.setEncounterStatus("IN_PROGRESS");
                    encounter.setStartTime(LocalDateTime.now());
                    encounterMapper.updateById(encounter);
                }
            }
        }

        log.info("Triage called: id={}, queueNo={}, deptId={}, doctorId={}",
                triage.getId(), triage.getQueueNo(), deptId, doctorId);
        return toVo(triage);
    }

    @Override
    @Transactional
    public TriageVo finish(Long triageId) {
        Triage triage = triageMapper.selectById(triageId);
        if (triage == null || triage.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.TRIAGE_NOT_FOUND);
        }
        triage.setTriageStatus("FINISHED");
        triage.setFinishTime(LocalDateTime.now());
        triageMapper.updateById(triage);
        log.info("Triage finished: id={}", triageId);
        return toVo(triage);
    }

    @Override
    public List<TriageVo> queueByDoctor(Long deptId, Long doctorId) {
        LambdaQueryWrapper<Triage> query = new LambdaQueryWrapper<>();
        query.eq(Triage::getDeptId, deptId)
                .in(Triage::getTriageStatus, "QUEUED", "CALLED")
                .eq(Triage::getDeleted, 0);
        if (doctorId != null) {
            query.eq(Triage::getDoctorId, doctorId);
        }
        query.orderByAsc(Triage::getQueueNo);
        return triageMapper.selectList(query).stream()
                .map(this::toVo)
                .toList();
    }

    @Override
    public int waitingCount(Long deptId, Long doctorId) {
        LambdaQueryWrapper<Triage> query = new LambdaQueryWrapper<>();
        query.eq(Triage::getDeptId, deptId)
                .eq(Triage::getTriageStatus, "QUEUED")
                .eq(Triage::getDeleted, 0);
        if (doctorId != null) {
            query.eq(Triage::getDoctorId, doctorId);
        }
        return Math.toIntExact(triageMapper.selectCount(query));
    }

    private Triage firstOrNull(LambdaQueryWrapper<Triage> query) {
        Page<Triage> page = new Page<>(1, 1);
        page.setSearchCount(false);
        List<Triage> records = triageMapper.selectPage(page, query).getRecords();
        return records.isEmpty() ? null : records.get(0);
    }

    private TriageVo toVo(Triage t) {
        TriageVo vo = new TriageVo();
        vo.setId(t.getId());
        vo.setRegId(t.getRegId());
        vo.setPatientId(t.getPatientId());
        vo.setDeptId(t.getDeptId());
        vo.setDoctorId(t.getDoctorId());
        vo.setVisitSeq(t.getVisitSeq());
        vo.setTriageStatus(t.getTriageStatus());
        vo.setQueueNo(t.getQueueNo());
        vo.setEnqueueTime(t.getEnqueueTime());
        vo.setCallTime(t.getCallTime());
        vo.setFinishTime(t.getFinishTime());
        if (t.getRegId() != null) {
            Registration reg = registrationMapper.selectById(t.getRegId());
            if (reg != null) {
                vo.setRegNo(reg.getRegNo());
            }
        }
        if (t.getPatientId() != null) {
            Patient patient = patientMapper.selectById(t.getPatientId());
            if (patient != null) {
                vo.setPatientName(patient.getName());
            }
        }
        if (t.getDoctorId() != null) {
            Doctor doc = doctorMapper.selectById(t.getDoctorId());
            if (doc != null) {
                vo.setDoctorName(doc.getDoctorName());
            }
        }
        if (t.getDeptId() != null) {
            Department dept = departmentMapper.selectById(t.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        return vo;
    }
}
