package com.smarthis.resource.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BedUpdateRequest {
    private String roomNo;
    private String bedType;
    private String bedRank;
    private Integer floorNo;
    private String bedStatus;
    private Integer isMale;
    private Long feeItemId;
    private BigDecimal dailyFee;
    private Integer sortOrder;
    private String remark;
}
