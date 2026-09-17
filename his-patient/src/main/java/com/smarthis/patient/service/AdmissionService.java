package com.smarthis.patient.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.patient.dto.request.AdmissionCreateRequest;
import com.smarthis.patient.dto.request.AdmissionDepositRequest;
import com.smarthis.patient.dto.request.AdmissionDischargeRequest;
import com.smarthis.patient.dto.request.AdmissionQueryRequest;
import com.smarthis.patient.dto.request.AdmissionTransferRequest;
import com.smarthis.patient.dto.response.AdmissionVo;

import java.util.List;

public interface AdmissionService {

    AdmissionVo create(AdmissionCreateRequest request);

    AdmissionVo getById(Long id);

    void admit(Long id);

    void discharge(Long id, AdmissionDischargeRequest request);

    AdmissionVo transfer(Long id, AdmissionTransferRequest request);

    void cancel(Long id);

    void addDeposit(Long id, AdmissionDepositRequest request);

    PageResult<AdmissionVo> list(AdmissionQueryRequest request);

    List<AdmissionVo> listByPatient(Long patientId);
}
