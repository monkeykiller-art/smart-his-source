package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCreateRequest {

    @NotNull(message = "settlement id is required")
    private Long settlementId;

    @NotNull(message = "pay method is required")
    private String payMethod;

    @NotNull(message = "pay amount is required")
    private BigDecimal payAmount;

    private String paySource;

    private String referenceNo;
}
