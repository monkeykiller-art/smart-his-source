package com.smarthis.patient.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AdmissionStatus {

    PLANNED("PLANNED", "待入院"),
    ADMITTED("ADMITTED", "在院"),
    DISCHARGED("DISCHARGED", "已出院"),
    CANCELLED("CANCELLED", "已取消");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    AdmissionStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
