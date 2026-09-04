package com.smarthis.clinical.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExamRequestItemRequest {

    private String itemCode;

    private String itemName;

    private String itemType;

    private String spec;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private String bodyPart;

    private String methodDesc;
}
