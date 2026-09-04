package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PrescriptionType {

    WESTERN("WESTERN", "西药"),
    TCM("TCM", "中药"),
    HERBAL("HERBAL", "草药");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    PrescriptionType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
