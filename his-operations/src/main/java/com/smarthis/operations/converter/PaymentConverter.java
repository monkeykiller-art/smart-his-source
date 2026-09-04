package com.smarthis.operations.converter;

import com.smarthis.operations.dto.response.PaymentVo;
import com.smarthis.operations.entity.Payment;

public final class PaymentConverter {

    private PaymentConverter() {
    }

    public static PaymentVo toVo(Payment e) {
        PaymentVo vo = new PaymentVo();
        vo.setId(e.getId());
        vo.setSettlementId(e.getSettlementId());
        vo.setPaymentNo(e.getPaymentNo());
        vo.setPayMethod(e.getPayMethod() != null ? e.getPayMethod().getValue() : null);
        vo.setPayAmount(e.getPayAmount());
        vo.setPaySource(e.getPaySource());
        vo.setReferenceNo(e.getReferenceNo());
        vo.setPayTime(e.getPayTime());
        vo.setPayStatus(e.getPayStatus());
        return vo;
    }
}
