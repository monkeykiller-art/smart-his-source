package com.smarthis.emergency.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TriageStatus {

    WAITING("WAITING", "等待"),
    IN_TREATMENT("IN_TREATMENT", "诊治中"),
    COMPLETED("COMPLETED", "完成"),
    CANCELLED("CANCELLED", "取消");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    TriageStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
