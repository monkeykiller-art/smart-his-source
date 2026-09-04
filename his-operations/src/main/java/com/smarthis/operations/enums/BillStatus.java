package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum BillStatus {
    UNSETTLED("UNSETTLED"),
    PARTIAL("PARTIAL"),
    SETTLED("SETTLED"),
    CANCELLED("CANCELLED");

    @EnumValue
    private final String value;

    BillStatus(String value) {
        this.value = value;
    }
}
