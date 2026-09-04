package com.smarthis.collaboration.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CarePlanStatus {
    DRAFT("DRAFT", "草稿"),
    ACTIVE("ACTIVE", "执行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    CarePlanStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
