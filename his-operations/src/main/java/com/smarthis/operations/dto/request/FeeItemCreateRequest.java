package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeItemCreateRequest {

    @NotBlank(message = "item code is required")
    private String itemCode;

    @NotBlank(message = "item name is required")
    private String itemName;

    private String namePinyin;

    @NotBlank(message = "item class is required")
    private String itemClass;

    private String itemCategory;

    private String spec;

    private String unit;

    @NotNull(message = "unit price is required")
    private BigDecimal unitPrice;

    private String dosageForm;

    private Integer isInsurance;

    private BigDecimal insuranceRatio;

    private Integer isSelfPay;

    private String executeDeptType;

    private Integer needConfirm;

    private Integer sortOrder;
}
