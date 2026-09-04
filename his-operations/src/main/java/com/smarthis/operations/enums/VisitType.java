package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum VisitType {
    OUTPATIENT("OUTPATIENT"),
    INPATIENT("INPATIENT"),
    EMERGENCY("EMERGENCY");

    @EnumValue
    private final String value;

    VisitType(String value) {
        this.value = value;
    }
}
