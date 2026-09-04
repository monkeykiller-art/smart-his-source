package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BedCreateRequest {
    @NotBlank private String bedNo;
    @NotNull private Long wardId;
    private String roomNo;
    private String bedType;
    private String bedRank;
    private Integer floorNo;
    private Integer isMale;
    private Long feeItemId;
    private BigDecimal dailyFee;
    private Integer sortOrder;
    private String remark;
}
