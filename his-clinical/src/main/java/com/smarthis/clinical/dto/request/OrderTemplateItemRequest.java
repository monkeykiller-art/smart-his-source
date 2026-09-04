package com.smarthis.clinical.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderTemplateItemRequest {

    private Integer groupNo;

    private String itemCode;

    private String itemName;

    private String spec;

    private BigDecimal dose;

    private String doseUnit;

    private String usageMethod;

    private String frequency;

    private Integer isFirstDay;

    private BigDecimal quantity;

    private String quantityUnit;

    private String dripRate;

    private Long executeDeptId;

    private String doctorAdvice;

    private String orderCategory;

    private Integer itemSeq;
}
