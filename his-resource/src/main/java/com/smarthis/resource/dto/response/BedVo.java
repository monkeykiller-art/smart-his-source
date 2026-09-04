package com.smarthis.resource.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BedVo {
    private Long id;
    private String bedNo;
    private Long wardId;
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
    private LocalDateTime createdTime;
}
