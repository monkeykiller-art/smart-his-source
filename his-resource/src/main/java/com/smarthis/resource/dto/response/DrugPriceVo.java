package com.smarthis.resource.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DrugPriceVo {
    private Long id;
    private Long drugId;
    private Long pharmacyId;
    private BigDecimal price;
    private BigDecimal retailPrice;
    private Integer isActive;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private LocalDateTime createdTime;
}
