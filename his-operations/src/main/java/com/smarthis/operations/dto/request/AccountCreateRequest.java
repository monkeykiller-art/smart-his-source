package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AccountCreateRequest {

    @NotNull(message = "cashier id is required")
    private String cashierId;

    private String cashierName;

    private String settleType;

    @NotNull(message = "total amount is required")
    private BigDecimal totalAmount;

    private BigDecimal cashAmount;

    private BigDecimal posAmount;

    private BigDecimal otherAmount;

    private Integer billCount;

    @NotNull(message = "account date is required")
    private LocalDate accountDate;

    private String remark;
}
