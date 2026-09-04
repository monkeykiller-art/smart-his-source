package com.smarthis.emergency.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GreenChannelStatus {

    ACTIVATED("ACTIVATED", "已激活"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CLOSED("CLOSED", "已关闭");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    GreenChannelStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
