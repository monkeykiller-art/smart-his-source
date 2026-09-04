package com.smarthis.collaboration.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ReferralStatus {
    PENDING("PENDING", "待接受"),
    ACCEPTED("ACCEPTED", "已接受"),
    REJECTED("REJECTED", "已拒绝"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    ReferralStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
