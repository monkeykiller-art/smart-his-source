package com.smarthis.patient.service;

import com.smarthis.patient.dto.response.EncounterVo;

import java.util.List;

public interface EncounterService {
    EncounterVo open(Long regId, String chiefComplaint);
    EncounterVo getById(Long id);
    EncounterVo getByRegId(Long regId);
    void close(Long id);
    List<EncounterVo> listByPatient(Long patientId);
}
