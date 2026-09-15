package com.smarthis.operations.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillTransactionVo {
    private Long id;
    private Long billId;
    private String transactionNo;
    private String transactionType;
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private BigDecimal amount;
    private String payMethod;
    private String referenceNo;
    private String reason;
    private LocalDateTime transactionTime;
    private String transactionStatus;
}
