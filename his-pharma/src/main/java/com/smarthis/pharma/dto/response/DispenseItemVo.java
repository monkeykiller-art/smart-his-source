package com.smarthis.pharma.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DispenseItemVo {
    private Long id;
    private Long prescriptionItemId;
    private Long drugId;
    private String drugCode;
    private String drugName;
    private String strength;
    private Long batchId;
    private String batchNo;
    private LocalDate expiryDate;
    private BigDecimal quantity;
    private String unit;
}
