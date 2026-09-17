package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DispenseItemRequest {
    private Long prescriptionItemId;
    @NotNull
    private Long drugId;
    @NotNull @DecimalMin("0.0001")
    private BigDecimal quantity;
    @NotBlank
    private String unit;
}
