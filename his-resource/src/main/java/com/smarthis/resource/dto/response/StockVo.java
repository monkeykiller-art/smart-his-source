package com.smarthis.resource.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockVo {
    private Long id;
    private Long drugId;
    private Long pharmacyId;
    private String batchNo;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private LocalDate produceDate;
    private LocalDate expiryDate;
    private Long supplierId;
    private String warehouseArea;
    private String stockStatus;
    private LocalDateTime createdTime;
}
