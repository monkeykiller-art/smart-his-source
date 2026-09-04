package com.smarthis.patient.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.patient.dto.request.PatientCreateRequest;
import com.smarthis.patient.dto.request.PatientQueryRequest;
import com.smarthis.patient.dto.request.PatientUpdateRequest;
import com.smarthis.patient.dto.response.PatientVo;

public interface PatientService {
    PatientVo create(PatientCreateRequest request);
    PatientVo getById(Long id);
    PatientVo getByIdNo(String idType, String idNo);
    PatientVo update(Long id, PatientUpdateRequest request);
    PageResult<PatientVo> query(PatientQueryRequest request);
}
