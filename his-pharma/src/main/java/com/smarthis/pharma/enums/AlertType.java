package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AlertType {

    INTERACTION("INTERACTION", "药物相互作用"),
    CONTRAINDICATION("CONTRAINDICATION", "禁忌"),
    DOSE_EXCEED("DOSE_EXCEED", "超量"),
    ALLERGY("ALLERGY", "过敏"),
    DUPLICATE("DUPLICATE", "重复用药"),
    FREQUENCY("FREQUENCY", "频次异常");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    AlertType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
