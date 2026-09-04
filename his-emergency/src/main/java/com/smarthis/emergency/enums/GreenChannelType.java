package com.smarthis.emergency.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GreenChannelType {

    STROKE("STROKE", "卒中"),
    CHEST_PAIN("CHEST_PAIN", "胸痛"),
    TRAUMA("TRAUMA", "创伤"),
    PREGNANCY("PREGNANCY", "孕产妇"),
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    GreenChannelType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
