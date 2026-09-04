package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AlertLevel {

    INFO("INFO", "信息"),
    WARNING("WARNING", "警告"),
    ERROR("ERROR", "错误"),
    FATAL("FATAL", "致命");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    AlertLevel(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
