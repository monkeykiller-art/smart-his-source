package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SettleType {
    INTERIM("INTERIM"),
    FINAL("FINAL"),
    ARREARS("ARREARS");

    @EnumValue
    private final String value;

    SettleType(String value) {
        this.value = value;
    }
}
