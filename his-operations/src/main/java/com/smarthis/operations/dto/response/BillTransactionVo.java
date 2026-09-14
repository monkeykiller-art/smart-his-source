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
    private BigDecimal amount;
    private String payMethod;
    private String referenceNo;
    private String reason;
    private LocalDateTime transactionTime;
    private String transactionStatus;
}
