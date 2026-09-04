package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.ExamRequestCreateRequest;
import com.smarthis.clinical.dto.response.ExamRequestVo;

import java.util.List;

public interface ExamRequestService {

    ExamRequestVo create(ExamRequestCreateRequest request);

    ExamRequestVo getById(Long id);

    void cancel(Long id);

    List<ExamRequestVo> listByPatient(Long patientId);

    List<ExamRequestVo> listByAdmission(Long admissionId);
}
