package com.smarthis.emergency.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ResuscitationType {

    CARDIAC_ARREST("CARDIAC_ARREST", "心搏骤停"),
    RESPIRATORY_FAILURE("RESPIRATORY_FAILURE", "呼吸衰竭"),
    SHOCK("SHOCK", "休克"),
    TRAUMA("TRAUMA", "创伤"),
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    ResuscitationType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
