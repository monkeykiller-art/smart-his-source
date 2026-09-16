package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.ExamRequestCreateRequest;
import com.smarthis.clinical.dto.response.ExamRequestVo;
import com.smarthis.clinical.dto.request.ExamRequestResultRequest;

import java.util.List;

public interface ExamRequestService {

    ExamRequestVo create(ExamRequestCreateRequest request);

    ExamRequestVo getById(Long id);

    void cancel(Long id);

    ExamRequestVo transition(Long id, String targetStatus);

    ExamRequestVo report(Long id, ExamRequestResultRequest request);

    ExamRequestVo acknowledgeCritical(Long id, Long userId);

    List<ExamRequestVo> listByPatient(Long patientId);

    List<ExamRequestVo> listByAdmission(Long admissionId);
}
