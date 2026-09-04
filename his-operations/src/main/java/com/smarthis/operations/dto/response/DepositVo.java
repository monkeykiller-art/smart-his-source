package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DepositVo {
    private Long id;
    private String depositNo;
    private Long patientId;
    private Long admissionId;
    private String receiptNo;
    private BigDecimal amount;
    private String payMethod;
    private String depositType;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String cashierId;
    private String cashierName;
    private LocalDateTime chargeTime;
    private String remark;
    private String depositStatus;
}
