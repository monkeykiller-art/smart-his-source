package com.smarthis.clinical.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVo {

    private Long id;

    private Long orderId;

    private Integer itemSeq;

    private String itemCode;

    private String itemName;

    private String itemType;

    private String spec;

    private BigDecimal dose;

    private String doseUnit;

    private String usageMethod;

    private String frequency;

    private Integer days;

    private BigDecimal quantity;

    private String quantityUnit;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private Integer isFirstDay;

    private String dripRate;

    private String skinTestResult;

    private String remark;

    private String itemStatus;
}
