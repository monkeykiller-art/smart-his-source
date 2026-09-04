package com.smarthis.collaboration.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ReferralType {
    UPWARD("UPWARD", "上转"),
    DOWNWARD("DOWNWARD", "下转"),
    LATERAL("LATERAL", "平转");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    ReferralType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
