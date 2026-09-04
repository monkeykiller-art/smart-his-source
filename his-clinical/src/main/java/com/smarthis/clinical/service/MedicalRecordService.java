package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.MedicalRecordCreateRequest;
import com.smarthis.clinical.dto.request.MedicalRecordUpdateRequest;
import com.smarthis.clinical.dto.response.MedicalRecordVo;

import java.util.List;

public interface MedicalRecordService {

    MedicalRecordVo create(MedicalRecordCreateRequest request);

    MedicalRecordVo update(Long id, MedicalRecordUpdateRequest request);

    MedicalRecordVo getById(Long id);

    void sign(Long id);

    List<MedicalRecordVo> listByPatient(Long patientId);

    List<MedicalRecordVo> listByEncounter(Long encounterId);
}
