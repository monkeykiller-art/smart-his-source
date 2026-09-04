package com.smarthis.resource.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DispenseItemVo {
    private Long id;
    private Long dispenseId;
    private Long drugId;
    private String drugCode;
    private String drugName;
    private String spec;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String batchNo;
    private String usageMethod;
    private String frequency;
    private Integer days;
    private String remark;
}
