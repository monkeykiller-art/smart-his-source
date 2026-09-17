package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventoryOperationRequest {
    @NotBlank
    private String operationType;
    @NotNull
    private Long drugId;
    private Long batchId;
    @NotBlank
    private String warehouseCode;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private BigDecimal unitCost;
    @NotNull @DecimalMin(value = "0.0000", inclusive = true)
    private BigDecimal quantity;
    private String referenceType;
    private Long referenceId;
    private Long operatorId;
    private String operatorName;
    private String reason;
}
