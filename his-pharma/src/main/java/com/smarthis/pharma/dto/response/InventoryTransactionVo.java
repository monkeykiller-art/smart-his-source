package com.smarthis.pharma.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryTransactionVo {
    private Long id;
    private String transactionNo;
    private String operationType;
    private Long drugId;
    private Long batchId;
    private String warehouseCode;
    private BigDecimal quantityChange;
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    private String referenceType;
    private Long referenceId;
    private Long operatorId;
    private String operatorName;
    private String reason;
    private LocalDateTime occurredTime;
}
