package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.patient.dto.response.DoctorVo;
import com.smarthis.patient.entity.Department;
import com.smarthis.patient.entity.Doctor;
import com.smarthis.patient.mapper.DepartmentMapper;
import com.smarthis.patient.mapper.DoctorMapper;
import com.smarthis.patient.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    @Cacheable(value = "deptDoctor", key = "#deptId == null ? 'ALL' : #deptId",
            unless = "#result == null || #result.isEmpty()")
    public List<DoctorVo> listByDept(Long deptId) {
        LambdaQueryWrapper<Doctor> query = new LambdaQueryWrapper<>();
        query.eq(Doctor::getDeleted, 0)
                .eq(Doctor::getDoctorStatus, "ACTIVE");
        if (deptId != null) {
            query.eq(Doctor::getDeptId, deptId);
        }
        query.orderByAsc(Doctor::getDoctorName);
        return doctorMapper.selectList(query).stream()
                .map(this::toVo)
                .toList();
    }

    @Override
    public DoctorVo getById(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null || doctor.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.DOCTOR_NOT_FOUND);
        }
        return toVo(doctor);
    }

    private DoctorVo toVo(Doctor d) {
        DoctorVo vo = new DoctorVo();
        vo.setId(d.getId());
        vo.setEmployeeNo(d.getEmployeeNo());
        vo.setDoctorName(d.getDoctorName());
        vo.setNamePinyin(d.getNamePinyin());
        vo.setGender(d.getGender());
        vo.setDeptId(d.getDeptId());
        vo.setTitle(d.getTitle());
        vo.setSpecialty(d.getSpecialty());
        vo.setPrescribeRight(d.getPrescribeRight());
        vo.setAntibioticLevel(d.getAntibioticLevel());
        vo.setDoctorStatus(d.getDoctorStatus());
        if (d.getDeptId() != null) {
            Department dept = departmentMapper.selectById(d.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        return vo;
    }
}
