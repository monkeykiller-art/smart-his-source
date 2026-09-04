package com.smarthis.emergency.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ResuscitationOutcome {

    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败"),
    TRANSFERRED("TRANSFERRED", "转院"),
    DEAD("DEAD", "死亡");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    ResuscitationOutcome(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
