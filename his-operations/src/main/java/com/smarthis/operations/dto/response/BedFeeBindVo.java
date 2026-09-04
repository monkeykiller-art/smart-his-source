package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BedFeeBindVo {
    private Long id;
    private Long bedId;
    private Long feeItemId;
    private String feeItemCode;
    private String feeItemName;
    private BigDecimal dailyFee;
    private Integer isActive;
}
