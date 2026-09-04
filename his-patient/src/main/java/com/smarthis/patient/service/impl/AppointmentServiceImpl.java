package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.patient.dto.request.AppointmentCreateRequest;
import com.smarthis.patient.dto.response.AppointmentVo;
import com.smarthis.patient.entity.Appointment;
import com.smarthis.patient.entity.Department;
import com.smarthis.patient.entity.Doctor;
import com.smarthis.patient.entity.Patient;
import com.smarthis.patient.entity.Schedule;
import com.smarthis.patient.mapper.AppointmentMapper;
import com.smarthis.patient.mapper.DepartmentMapper;
import com.smarthis.patient.mapper.DoctorMapper;
import com.smarthis.patient.mapper.PatientMapper;
import com.smarthis.patient.mapper.ScheduleMapper;
import com.smarthis.patient.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final PatientMapper patientMapper;
    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public AppointmentVo create(AppointmentCreateRequest request) {
        Patient patient = patientMapper.selectById(request.getPatientId());
        if (patient == null || patient.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null || schedule.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        if (!"ACTIVE".equals(schedule.getScheduleStatus())) {
            throw new BusinessException(ErrorCode.SCHEDULE_NO_QUOTA);
        }

        LambdaQueryWrapper<Appointment> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(Appointment::getPatientId, request.getPatientId())
                .eq(Appointment::getScheduleId, request.getScheduleId())
                .in(Appointment::getApptStatus, "PENDING", "CONFIRMED")
                .eq(Appointment::getDeleted, 0);
        if (appointmentMapper.selectCount(existQuery) > 0) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CONFLICT);
        }

        Appointment appt = new Appointment();
        appt.setApptNo(bizNoGenerator.next(BizNoType.REGISTRATION).replace("MZ", "YY"));
        appt.setPatientId(request.getPatientId());
        appt.setScheduleId(request.getScheduleId());
        appt.setDeptId(schedule.getDeptId());
        appt.setDoctorId(schedule.getDoctorId());
        appt.setApptDate(schedule.getScheduleDate());
        appt.setTimePeriod(schedule.getTimePeriod());
        appt.setApptSource(request.getApptSource() != null ? request.getApptSource() : "ONLINE");
        appt.setApptStatus("PENDING");
        appointmentMapper.insert(appt);

        log.info("Appointment created: apptNo={}, patientId={}, scheduleId={}",
                appt.getApptNo(), appt.getPatientId(), appt.getScheduleId());
        return toVo(appt);
    }

    @Override
    public AppointmentVo getById(Long id) {
        return toVo(loadRequired(id));
    }

    @Override
    @Transactional
    public void confirm(Long id) {
        Appointment appt = loadRequired(id);
        if ("CANCELLED".equals(appt.getApptStatus())) {
            throw new BusinessException(ErrorCode.APPOINTMENT_CONFLICT.getCode(),
                    "appointment already cancelled");
        }
        appt.setApptStatus("CONFIRMED");
        appt.setConfirmTime(LocalDateTime.now());
        appointmentMapper.updateById(appt);
        log.info("Appointment confirmed: id={}", id);
    }

    @Override
    @Transactional
    public void cancel(Long id, String reason) {
        Appointment appt = loadRequired(id);
        appt.setApptStatus("CANCELLED");
        appt.setCancelReason(reason);
        appt.setCancelTime(LocalDateTime.now());
        appointmentMapper.updateById(appt);
        log.info("Appointment cancelled: id={}, reason={}", id, reason);
    }

    @Override
    public List<AppointmentVo> listByPatient(Long patientId) {
        LambdaQueryWrapper<Appointment> query = new LambdaQueryWrapper<>();
        query.eq(Appointment::getPatientId, patientId)
                .eq(Appointment::getDeleted, 0)
                .orderByDesc(Appointment::getApptDate);
        return appointmentMapper.selectList(query).stream()
                .map(this::toVo)
                .toList();
    }

    private Appointment loadRequired(Long id) {
        Appointment appt = appointmentMapper.selectById(id);
        if (appt == null || appt.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "appointment not found");
        }
        return appt;
    }

    private AppointmentVo toVo(Appointment a) {
        AppointmentVo vo = new AppointmentVo();
        vo.setId(a.getId());
        vo.setApptNo(a.getApptNo());
        vo.setPatientId(a.getPatientId());
        vo.setScheduleId(a.getScheduleId());
        vo.setDeptId(a.getDeptId());
        vo.setDoctorId(a.getDoctorId());
        vo.setApptDate(a.getApptDate());
        vo.setTimePeriod(a.getTimePeriod());
        vo.setApptSource(a.getApptSource());
        vo.setApptStatus(a.getApptStatus());
        vo.setConfirmTime(a.getConfirmTime());
        vo.setRegId(a.getRegId());
        if (a.getPatientId() != null) {
            Patient patient = patientMapper.selectById(a.getPatientId());
            if (patient != null) {
                vo.setPatientName(patient.getName());
            }
        }
        if (a.getDoctorId() != null) {
            Doctor doc = doctorMapper.selectById(a.getDoctorId());
            if (doc != null) {
                vo.setDoctorName(doc.getDoctorName());
            }
        }
        if (a.getDeptId() != null) {
            Department dept = departmentMapper.selectById(a.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        return vo;
    }
}
