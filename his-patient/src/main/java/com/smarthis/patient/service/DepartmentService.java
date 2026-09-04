package com.smarthis.patient.service;

import com.smarthis.patient.dto.response.DepartmentVo;
import java.util.List;

public interface DepartmentService {
    List<DepartmentVo> listAll();
    DepartmentVo getById(Long id);
}
