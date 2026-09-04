package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRefundRequest {

    @NotNull(message = "amount is required")
    private BigDecimal amount;

    private String cashierId;

    private String cashierName;

    private String remark;
}
