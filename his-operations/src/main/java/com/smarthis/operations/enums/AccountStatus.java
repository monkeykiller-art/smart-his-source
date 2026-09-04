package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AccountStatus {
    PENDING("PENDING"),
    SUBMITTED("SUBMITTED"),
    RECEIVED("RECEIVED"),
    CANCELLED("CANCELLED");

    @EnumValue
    private final String value;

    AccountStatus(String value) {
        this.value = value;
    }
}
