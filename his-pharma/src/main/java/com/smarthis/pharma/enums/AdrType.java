package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AdrType {

    SIDE_EFFECT("SIDE_EFFECT", "副作用"),
    ALLERGY("ALLERGY", "过敏反应"),
    TOXICITY("TOXICITY", "毒性反应"),
    INTERACTION("INTERACTION", "相互作用"),
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    AdrType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
