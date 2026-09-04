package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ReviewResult {

    PASS("PASS", "通过"),
    WARNING("WARNING", "有警告"),
    ERROR("ERROR", "有错误");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    ReviewResult(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
