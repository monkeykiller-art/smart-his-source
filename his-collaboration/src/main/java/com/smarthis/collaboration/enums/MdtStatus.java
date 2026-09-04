package com.smarthis.collaboration.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MdtStatus {
    APPLIED("APPLIED", "已申请"),
    SCHEDULED("SCHEDULED", "已安排"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    MdtStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
