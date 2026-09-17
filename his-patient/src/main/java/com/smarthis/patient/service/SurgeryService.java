package com.smarthis.patient.service;

import com.smarthis.patient.dto.request.SurgeryApplyRequest;
import com.smarthis.patient.dto.request.SurgeryCompleteRequest;
import com.smarthis.patient.dto.request.SurgeryScheduleRequest;
import com.smarthis.patient.entity.SurgeryCase;

import java.util.List;

public interface SurgeryService {
    SurgeryCase apply(SurgeryApplyRequest request);
    SurgeryCase schedule(Long id, SurgeryScheduleRequest request);
    SurgeryCase start(Long id);
    SurgeryCase complete(Long id, SurgeryCompleteRequest request);
    List<SurgeryCase> list(Long admissionId);
}
