package com.smarthis.pharma.dto.response;

import lombok.Data;

@Data
public class DrugAllergyCrossVo {

    private Long id;

    private String allergyCode;

    private String allergyName;

    private String crossDrugCode;

    private String crossDrugName;

    private String crossLevel;

    private String description;

    private Integer isActive;
}
