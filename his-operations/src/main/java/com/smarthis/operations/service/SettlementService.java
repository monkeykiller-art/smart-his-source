package com.smarthis.operations.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.SettlementCancelRequest;
import com.smarthis.operations.dto.request.SettlementCreateRequest;
import com.smarthis.operations.dto.request.SettlementQueryRequest;
import com.smarthis.operations.dto.response.SettlementPreviewVo;
import com.smarthis.operations.dto.response.SettlementVo;

public interface SettlementService {
    SettlementVo create(SettlementCreateRequest request);
    SettlementVo getById(Long id);
    PageResult<SettlementVo> query(SettlementQueryRequest request);
    SettlementVo cancel(Long id, SettlementCancelRequest request);
    SettlementPreviewVo preview(Long admissionId);
}
