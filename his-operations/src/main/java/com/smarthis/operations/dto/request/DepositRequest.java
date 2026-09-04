package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {

    @NotNull(message = "admissionId is required")
    private Long admissionId;

    @NotNull(message = "patientId is required")
    private Long patientId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "payMethod is required")
    private String payMethod;

    private String remark;
}
