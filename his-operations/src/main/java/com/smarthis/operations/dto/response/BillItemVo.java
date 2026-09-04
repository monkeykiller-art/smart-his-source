package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillItemVo {
    private Long id;
    private Long billId;
    private Integer itemSeq;
    private Long feeItemId;
    private String itemCode;
    private String itemName;
    private String itemClass;
    private String spec;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal amount;
    private Long chargeDeptId;
    private Long executeDeptId;
    private Long orderId;
    private Long orderItemId;
    private LocalDateTime prescTime;
    private LocalDateTime chargeTime;
    private String chargerId;
    private Integer isRefunded;
    private String itemStatus;
    private String remark;
}
