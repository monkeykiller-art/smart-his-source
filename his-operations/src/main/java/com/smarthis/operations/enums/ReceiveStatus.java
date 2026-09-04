package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ReceiveStatus {
    UNRECEIVED("UNRECEIVED"),
    RECEIVED("RECEIVED");

    @EnumValue
    private final String value;

    ReceiveStatus(String value) {
        this.value = value;
    }
}
