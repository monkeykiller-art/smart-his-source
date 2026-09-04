package com.smarthis.patient.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AdmissionType {

    ELECTIVE("ELECTIVE", "择期"),
    EMERGENCY("EMERGENCY", "急诊"),
    TRANSFER("TRANSFER", "转院");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    AdmissionType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
