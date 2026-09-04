package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DrugPriceCreateRequest {
    @NotNull private Long drugId;
    @NotNull private Long pharmacyId;
    @NotNull private BigDecimal price;
    @NotNull private BigDecimal retailPrice;
    private Integer isActive;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
}
