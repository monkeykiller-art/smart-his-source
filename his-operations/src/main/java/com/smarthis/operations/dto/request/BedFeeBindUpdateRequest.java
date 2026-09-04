package com.smarthis.operations.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BedFeeBindUpdateRequest {

    private String feeItemCode;

    private String feeItemName;

    private BigDecimal dailyFee;

    private Integer isActive;
}
