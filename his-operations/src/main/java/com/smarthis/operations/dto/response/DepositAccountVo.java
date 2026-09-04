package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositAccountVo {
    private Long id;
    private Long patientId;
    private Long admissionId;
    private BigDecimal totalDeposit;
    private BigDecimal totalCharged;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private String accountStatus;
}
