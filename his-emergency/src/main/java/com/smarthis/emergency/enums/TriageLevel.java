package com.smarthis.emergency.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TriageLevel {

    L1("1", "濒死"),
    L2("2", "危重"),
    L3("3", "急症"),
    L4("4", "非急症");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    TriageLevel(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
