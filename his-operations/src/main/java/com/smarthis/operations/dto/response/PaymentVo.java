package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVo {
    private Long id;
    private Long settlementId;
    private String paymentNo;
    private String payMethod;
    private BigDecimal payAmount;
    private String paySource;
    private String referenceNo;
    private LocalDateTime payTime;
    private String payStatus;
}
