package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillVo {
    private Long id;
    private String billNo;
    private String invoiceNo;
    private String sourceType;
    private Long sourceId;
    private Long patientId;
    private Long admissionId;
    private Long encounterId;
    private String visitType;
    private Long deptId;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private BigDecimal totalAmount;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private BigDecimal discountAmount;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private BigDecimal payableAmount;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private BigDecimal paidAmount;
    private String billStatus;
    private String billType;
    private String remark;
    private String voidReason;
    private LocalDateTime createdTime;
}
