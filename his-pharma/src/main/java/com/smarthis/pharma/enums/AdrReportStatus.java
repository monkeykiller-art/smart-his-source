package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AdrReportStatus {

    SUBMITTED("SUBMITTED", "已提交"),
    UNDER_REVIEW("UNDER_REVIEW", "审核中"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝"),
    REPORTED("REPORTED", "已上报");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    AdrReportStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
