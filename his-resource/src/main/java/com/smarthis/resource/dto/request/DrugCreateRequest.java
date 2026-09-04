package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrugCreateRequest {
    @NotBlank private String drugCode;
    @NotBlank private String drugName;
    private String namePinyin;
    private String genericName;
    private String dosageForm;
    private String spec;
    private String unit;
    private String packUnit;
    private BigDecimal packQty;
    private String manufacturer;
    private String approvalNo;
    private String barCode;
    private String drugType;
    private Integer isInsurance;
    private BigDecimal insuranceRatio;
    private Integer isNarcotic;
    private Integer isPsychotropic;
    private Integer isAntibiotic;
    private Integer antibioticLevel;
    private Integer needSkinTest;
    private BigDecimal maxSingleDose;
    private BigDecimal maxDailyDose;
    private String storageCondition;
    private Integer sortOrder;
}
