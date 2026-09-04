package com.smarthis.operations.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ItemClass {
    BED("BED"),
    NURSING("NURSING"),
    DRUG("DRUG"),
    LAB("LAB"),
    IMAGING("IMAGING"),
    SURGERY("SURGERY"),
    TREATMENT("TREATMENT"),
    MATERIAL("MATERIAL"),
    OTHER("OTHER");

    @EnumValue
    private final String value;

    ItemClass(String value) {
        this.value = value;
    }
}
