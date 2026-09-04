package com.smarthis.clinical.dto.response;

import lombok.Data;

@Data
public class Icd10Vo {

    private Long id;

    private String icdCode;

    private String icdName;

    private String namePinyin;

    private String chapter;

    private String block;

    private String category;

    private String subCategory;

    private Integer isInfectious;

    private Integer isChronic;

    private Integer isTcm;

    private Integer dictStatus;
}
