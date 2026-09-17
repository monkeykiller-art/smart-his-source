package com.smarthis.pharma.service;

import com.smarthis.pharma.dto.request.DispenseCreateRequest;
import com.smarthis.pharma.dto.response.DispenseVo;

import java.util.List;

public interface DispenseService {
    DispenseVo dispense(DispenseCreateRequest request);
    DispenseVo getById(Long id);
    List<DispenseVo> listByPatient(Long patientId);
    DispenseVo returnAll(Long id, Long operatorId, String operatorName, String reason);
}
