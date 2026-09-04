package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.patient.converter.EncounterConverter;
import com.smarthis.patient.dto.response.EncounterVo;
import com.smarthis.patient.entity.Department;
import com.smarthis.patient.entity.Doctor;
import com.smarthis.patient.entity.Encounter;
import com.smarthis.patient.entity.Patient;
import com.smarthis.patient.entity.Registration;
import com.smarthis.patient.mapper.DepartmentMapper;
import com.smarthis.patient.mapper.DoctorMapper;
import com.smarthis.patient.mapper.EncounterMapper;
import com.smarthis.patient.mapper.PatientMapper;
import com.smarthis.patient.mapper.RegistrationMapper;
import com.smarthis.patient.service.EncounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncounterServiceImpl implements EncounterService {

    private final EncounterMapper encounterMapper;
    private final RegistrationMapper registrationMapper;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public EncounterVo open(Long regId, String chiefComplaint) {
        Registration reg = registrationMapper.selectById(regId);
        if (reg == null || reg.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }
        if ("CANCELLED".equals(reg.getRegStatus())) {
            throw new BusinessException(ErrorCode.REGISTRATION_CANCELLED);
        }

        Encounter existing = findByRegId(regId);
        if (existing != null) {
            if ("CLOSED".equals(existing.getEncounterStatus())) {
                throw new BusinessException(ErrorCode.ENCOUNTER_CLOSED);
            }
            // Re-opening a planned encounter simply activates it
            if ("PLANNED".equals(existing.getEncounterStatus())) {
                existing.setEncounterStatus("IN_PROGRESS");
                existing.setStartTime(LocalDateTime.now());
            }
            if (chiefComplaint != null && !chiefComplaint.isBlank()) {
                existing.setChiefComplaint(chiefComplaint);
            }
            encounterMapper.updateById(existing);
            return toVo(existing);
        }

        Encounter encounter = new Encounter();
        encounter.setEncounterNo(bizNoGenerator.next(BizNoType.REGISTRATION).replace("MZ", "JZ"));
        encounter.setPatientId(reg.getPatientId());
        encounter.setRegId(regId);
        encounter.setDeptId(reg.getDeptId());
        encounter.setDoctorId(reg.getDoctorId());
        encounter.setEncounterType("OUTPATIENT");
        encounter.setEncounterStatus("IN_PROGRESS");
        encounter.setVisitDate(LocalDate.now());
        encounter.setStartTime(LocalDateTime.now());
        encounter.setChiefComplaint(chiefComplaint);
        encounterMapper.insert(encounter);
        log.info("Encounter opened: encounterNo={}, regId={}", encounter.getEncounterNo(), regId);
        return toVo(encounter);
    }

    @Override
    public EncounterVo getById(Long id) {
        Encounter encounter = encounterMapper.selectById(id);
        if (encounter == null || encounter.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.ENCOUNTER_NOT_FOUND);
        }
        return toVo(encounter);
    }

    @Override
    public EncounterVo getByRegId(Long regId) {
        Encounter encounter = findByRegId(regId);
        if (encounter == null) {
            throw new BusinessException(ErrorCode.ENCOUNTER_NOT_FOUND);
        }
        return toVo(encounter);
    }

    @Override
    @Transactional
    public void close(Long id) {
        Encounter encounter = encounterMapper.selectById(id);
        if (encounter == null || encounter.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.ENCOUNTER_NOT_FOUND);
        }
        if ("CLOSED".equals(encounter.getEncounterStatus())) {
            throw new BusinessException(ErrorCode.ENCOUNTER_CLOSED);
        }
        encounter.setEncounterStatus("CLOSED");
        encounter.setEndTime(LocalDateTime.now());
        encounterMapper.updateById(encounter);
        log.info("Encounter closed: id={}", id);
    }

    @Override
    public List<EncounterVo> listByPatient(Long patientId) {
        LambdaQueryWrapper<Encounter> query = new LambdaQueryWrapper<>();
        query.eq(Encounter::getPatientId, patientId)
                .eq(Encounter::getDeleted, 0)
                .orderByDesc(Encounter::getVisitDate);
        return encounterMapper.selectList(query).stream()
                .map(this::toVo)
                .toList();
    }

    private Encounter findByRegId(Long regId) {
        LambdaQueryWrapper<Encounter> query = new LambdaQueryWrapper<>();
        query.eq(Encounter::getRegId, regId)
                .eq(Encounter::getDeleted, 0)
                .orderByDesc(Encounter::getId);
        Page<Encounter> page = new Page<>(1, 1);
        page.setSearchCount(false);
        List<Encounter> records = encounterMapper.selectPage(page, query).getRecords();
        return records.isEmpty() ? null : records.get(0);
    }

    private EncounterVo toVo(Encounter e) {
        EncounterVo vo = EncounterConverter.toVo(e);
        if (e.getPatientId() != null) {
            Patient patient = patientMapper.selectById(e.getPatientId());
            if (patient != null) {
                vo.setPatientName(patient.getName());
            }
        }
        if (e.getDoctorId() != null) {
            Doctor doc = doctorMapper.selectById(e.getDoctorId());
            if (doc != null) {
                vo.setDoctorName(doc.getDoctorName());
            }
        }
        if (e.getDeptId() != null) {
            Department dept = departmentMapper.selectById(e.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        if (e.getRegId() != null) {
            Registration reg = registrationMapper.selectById(e.getRegId());
            if (reg != null) {
                vo.setRegNo(reg.getRegNo());
            }
        }
        return vo;
    }
}
