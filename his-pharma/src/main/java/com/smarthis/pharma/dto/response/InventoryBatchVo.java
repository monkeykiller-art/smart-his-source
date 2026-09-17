package com.smarthis.pharma.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventoryBatchVo {
    private Long id;
    private Long drugId;
    private String drugCode;
    private String drugName;
    private String warehouseCode;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private BigDecimal unitCost;
    private BigDecimal quantity;
    private BigDecimal availableQuantity;
    private BigDecimal lockedQuantity;
    private boolean nearExpiry;
    private boolean expired;
}
