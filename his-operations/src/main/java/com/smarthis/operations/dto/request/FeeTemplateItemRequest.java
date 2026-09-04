package com.smarthis.operations.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeTemplateItemRequest {

    private String itemType;

    private String itemCode;

    private String itemName;

    private BigDecimal quantity;

    private String unit;

    private Long executeDeptId;

    private Integer itemSeq;
}
