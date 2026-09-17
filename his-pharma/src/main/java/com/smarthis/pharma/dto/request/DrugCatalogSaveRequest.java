package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrugCatalogSaveRequest {
    @NotBlank @Size(max = 32)
    private String drugCode;
    @NotBlank @Size(max = 128)
    private String genericName;
    @Size(max = 128)
    private String tradeName;
    @Size(max = 32)
    private String pinyinCode;
    @NotBlank @Size(max = 32)
    private String dosageForm;
    @NotBlank @Size(max = 64)
    private String strength;
    @NotBlank @Size(max = 128)
    private String manufacturer;
    @Size(max = 64)
    private String approvalNo;
    @NotBlank @Size(max = 16)
    private String packageUnit;
    @NotBlank @Size(max = 16)
    private String minUnit;
    @NotNull @DecimalMin("0.0001")
    private BigDecimal conversionFactor;
    @NotNull @DecimalMin("0.0000")
    private BigDecimal purchasePrice;
    @NotNull @DecimalMin("0.0000")
    private BigDecimal retailPrice;
    @NotBlank
    private String prescriptionType;
    private String antibioticLevel;
    private Integer isActive;
}
