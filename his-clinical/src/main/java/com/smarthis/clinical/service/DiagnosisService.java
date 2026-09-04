package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.DiagnosisCreateRequest;
import com.smarthis.clinical.dto.response.DiagnosisVo;

import java.util.List;

public interface DiagnosisService {

    DiagnosisVo create(DiagnosisCreateRequest request);

    DiagnosisVo update(Long id, DiagnosisCreateRequest request);

    void delete(Long id);

    List<DiagnosisVo> listByEncounter(Long encounterId);

    List<DiagnosisVo> listByAdmission(Long admissionId);
}
