package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PayMethod {
    CASH("CASH"),
    POS("POS"),
    WECHAT("WECHAT"),
    ALIPAY("ALIPAY"),
    INSURANCE("INSURANCE"),
    MIXED("MIXED");

    @EnumValue
    private final String value;

    PayMethod(String value) {
        this.value = value;
    }
}
