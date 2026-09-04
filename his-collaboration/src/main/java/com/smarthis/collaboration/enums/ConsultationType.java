package com.smarthis.collaboration.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ConsultationType {
    ROUTINE("ROUTINE", "常规会诊"),
    URGENT("URGENT", "急会诊"),
    REMOTE("REMOTE", "远程会诊");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    ConsultationType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
