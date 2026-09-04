package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargeRequest {

    @NotNull(message = "patientId is required")
    private Long patientId;

    private Long admissionId;

    private Long encounterId;

    private String visitType;

    @NotNull(message = "feeItemId is required")
    private Long feeItemId;

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.0001", message = "quantity must be positive")
    private BigDecimal quantity;

    private Long deptId;

    private Long orderId;

    private Long orderItemId;

    private String remark;
}
