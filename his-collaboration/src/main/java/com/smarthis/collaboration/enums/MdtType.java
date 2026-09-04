package com.smarthis.collaboration.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MdtType {
    ROUTINE("ROUTINE", "常规MDT"),
    URGENT("URGENT", "紧急MDT");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    MdtType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
