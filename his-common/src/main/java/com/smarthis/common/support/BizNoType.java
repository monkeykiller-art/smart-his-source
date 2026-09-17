package com.smarthis.common.support;

import lombok.Getter;

@Getter
public enum BizNoType {

    REGISTRATION("MZ", "门诊挂号"),
    PRESCRIPTION("RX", "处方"),
    BILL("BL", "账单"),
    DISPENSE("DP", "发药"),
    ADMISSION("ZY", "住院"),
    SURGERY("SS", "手术"),
    EMERGENCY("JZ", "急诊"),
    PHYSICAL_EXAM("TJ", "体检"),
    ORDER("YZ", "医嘱"),
    EXAM_REQUEST("SQ", "检查检验申请"),
    EXAM_REPORT("BG", "检查检验报告"),
    CLINICAL_RECORD("BA", "病历"),
    TRANSFER("ZZ", "转科"),
    PAYMENT("SK", "收款"),
    REFUND("TK", "退款"),
    INVOICE("FP", "发票"),
    INSURANCE_PREAUTH("YB", "医保预授权"),
    INSURANCE_CLAIM("JS", "医保结算"),
    DEPOSIT("YJ", "押金"),
    SETTLEMENT("JF", "结算"),
    ACCOUNT("JK", "缴款"),
    RX_REVIEW("YP", "处方审核"),
    ADR_REPORT("BL", "ADR报告");

    private final String prefix;
    private final String description;

    BizNoType(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
    }
}
