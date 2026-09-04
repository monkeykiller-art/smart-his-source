package com.smarthis.emergency.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ObservationStatus {

    ADMITTED("ADMITTED", "已留观"),
    DISCHARGED("DISCHARGED", "已出观"),
    TRANSFERRED("TRANSFERRED", "已转科"),
    CANCELLED("CANCELLED", "已取消");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    ObservationStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
