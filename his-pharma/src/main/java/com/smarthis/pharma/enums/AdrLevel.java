package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AdrLevel {

    MILD("MILD", "轻度"),
    MODERATE("MODERATE", "中度"),
    SEVERE("SEVERE", "重度"),
    FATAL("FATAL", "致死");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    AdrLevel(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
