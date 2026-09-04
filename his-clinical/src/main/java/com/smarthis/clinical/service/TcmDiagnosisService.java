package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.TcmDiagnosisQueryRequest;
import com.smarthis.clinical.dto.response.TcmDiagnosisVo;
import com.smarthis.common.model.PageResult;

public interface TcmDiagnosisService {
    TcmDiagnosisVo getById(Long id);
    PageResult<TcmDiagnosisVo> query(TcmDiagnosisQueryRequest request);
}
