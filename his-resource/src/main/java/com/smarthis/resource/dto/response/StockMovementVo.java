package com.smarthis.resource.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockMovementVo {
    private Long id;
    private String movementNo;
    private Long drugId;
    private Long pharmacyId;
    private String batchNo;
    private String movementType;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalAmount;
    private BigDecimal beforeQty;
    private BigDecimal afterQty;
    private String referenceType;
    private Long referenceId;
    private Long operatorId;
    private LocalDateTime movementTime;
    private String remark;
    private LocalDateTime createdTime;
}
