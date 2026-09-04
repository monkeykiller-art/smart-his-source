package com.smarthis.patient.service;

import com.smarthis.patient.dto.response.DoctorVo;

import java.util.List;

public interface DoctorService {
    List<DoctorVo> listByDept(Long deptId);
    DoctorVo getById(Long id);
}
