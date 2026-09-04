package com.smarthis.collaboration.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CarePlanType {
    NURSING("NURSING", "护理计划"),
    REHABILITATION("REHABILITATION", "康复计划"),
    NUTRITION("NUTRITION", "营养计划"),
    PSYCHOLOGICAL("PSYCHOLOGICAL", "心理计划"),
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    CarePlanType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
