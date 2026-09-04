package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum BillType {
    NORMAL("NORMAL"),
    INTERIM("INTERIM"),
    FINAL("FINAL");

    @EnumValue
    private final String value;

    BillType(String value) {
        this.value = value;
    }
}
