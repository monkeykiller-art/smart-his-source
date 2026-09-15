package com.smarthis.patient.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.patient.dto.request.RegistrationCreateRequest;
import com.smarthis.patient.dto.request.RegistrationQueryRequest;
import com.smarthis.patient.dto.response.RegistrationVo;

public interface RegistrationService {
    RegistrationVo create(RegistrationCreateRequest request);
    RegistrationVo getById(Long id);
    void cancel(Long id, String reason);
    void markPaid(Long id, Long billId);
    void refund(Long id, String reason);
    void syncBilling(Long id);
    PageResult<RegistrationVo> query(RegistrationQueryRequest request);
}
