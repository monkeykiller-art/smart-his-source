package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositCreateRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    @NotNull(message = "admission id is required")
    private Long admissionId;

    @NotNull(message = "amount is required")
    private BigDecimal amount;

    @NotNull(message = "pay method is required")
    private String payMethod;

    private String receiptNo;

    private String cashierId;

    private String cashierName;

    private String remark;
}
