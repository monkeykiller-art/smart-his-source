package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ReviewStatus {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "审核通过"),
    REJECTED("REJECTED", "审核拒绝");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    ReviewStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
