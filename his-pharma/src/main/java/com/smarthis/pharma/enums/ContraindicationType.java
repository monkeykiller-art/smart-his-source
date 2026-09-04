package com.smarthis.pharma.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ContraindicationType {

    ALLERGY("ALLERGY", "过敏"),
    PREGNANCY("PREGNANCY", "妊娠"),
    LACTATION("LACTATION", "哺乳"),
    DISEASE("DISEASE", "疾病"),
    AGE("AGE", "年龄"),
    DRUG("DRUG", "药物");

    @EnumValue
    @JsonValue
    private final String value;
    private final String desc;

    ContraindicationType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
