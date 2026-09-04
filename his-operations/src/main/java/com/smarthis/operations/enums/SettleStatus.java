package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SettleStatus {
    SETTLED("SETTLED"),
    CANCELLED("CANCELLED");

    @EnumValue
    private final String value;

    SettleStatus(String value) {
        this.value = value;
    }
}
