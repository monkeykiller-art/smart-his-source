package com.smarthis.pharma.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrugCatalogVo {
    private Long id;
    private String drugCode;
    private String genericName;
    private String tradeName;
    private String pinyinCode;
    private String dosageForm;
    private String strength;
    private String manufacturer;
    private String approvalNo;
    private String packageUnit;
    private String minUnit;
    private BigDecimal conversionFactor;
    private BigDecimal purchasePrice;
    private BigDecimal retailPrice;
    private String prescriptionType;
    private String antibioticLevel;
    private Integer isActive;
}
