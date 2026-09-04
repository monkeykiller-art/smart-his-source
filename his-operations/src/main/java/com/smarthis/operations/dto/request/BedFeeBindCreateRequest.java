package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BedFeeBindCreateRequest {

    @NotNull(message = "bed id is required")
    private Long bedId;

    @NotNull(message = "fee item id is required")
    private Long feeItemId;

    private String feeItemCode;

    private String feeItemName;

    @NotNull(message = "daily fee is required")
    private BigDecimal dailyFee;
}
