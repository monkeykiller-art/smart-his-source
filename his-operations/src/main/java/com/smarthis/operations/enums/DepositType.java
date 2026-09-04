package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum DepositType {
    PAYMENT("PAYMENT"),
    REFUND("REFUND");

    @EnumValue
    private final String value;

    DepositType(String value) {
        this.value = value;
    }
}
