package com.smarthis.operations.service;

import com.smarthis.operations.dto.request.PaymentCreateRequest;
import com.smarthis.operations.dto.response.PaymentVo;

import java.util.List;

public interface PaymentService {
    PaymentVo create(PaymentCreateRequest request);
    List<PaymentVo> queryBySettlementId(Long settlementId);
}
