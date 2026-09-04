package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SettlementItemVo {
    private Long id;
    private Long settlementId;
    private String itemClass;
    private Integer itemCount;
    private BigDecimal totalAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal selfPayAmount;
}
