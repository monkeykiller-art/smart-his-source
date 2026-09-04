package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AdrOutcome {

    RECOVERED("RECOVERED", "痊愈"),
    RECOVERING("RECOVERING", "好转"),
    NOT_RECOVERED("NOT_RECOVERED", "未好转"),
    SEQUELA("SEQUELA", "后遗症"),
    DEATH("DEATH", "死亡"),
    UNKNOWN("UNKNOWN", "未知");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    AdrOutcome(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
