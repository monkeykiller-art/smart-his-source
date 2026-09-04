package com.smarthis.operations.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.DepositCreateRequest;
import com.smarthis.operations.dto.request.DepositQueryRequest;
import com.smarthis.operations.dto.request.DepositRefundRequest;
import com.smarthis.operations.dto.response.DepositVo;

public interface DepositService {
    DepositVo create(DepositCreateRequest request);
    DepositVo getById(Long id);
    PageResult<DepositVo> query(DepositQueryRequest request);
    DepositVo refund(Long id, DepositRefundRequest request);
}
