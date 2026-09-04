package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockMovementCreateRequest {
    @NotNull private Long drugId;
    @NotNull private Long pharmacyId;
    private String batchNo;
    @NotNull private String movementType;
    @NotNull private BigDecimal quantity;
    private BigDecimal unitCost;
    private String referenceType;
    private Long referenceId;
    private Long operatorId;
    private LocalDateTime movementTime;
    private String remark;
}
