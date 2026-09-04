package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AccountVo {
    private Long id;
    private String accountNo;
    private String cashierId;
    private String cashierName;
    private String settleType;
    private BigDecimal totalAmount;
    private BigDecimal cashAmount;
    private BigDecimal posAmount;
    private BigDecimal otherAmount;
    private Integer billCount;
    private LocalDate accountDate;
    private LocalDateTime submitTime;
    private LocalDateTime receiveTime;
    private String receiverId;
    private String accountStatus;
    private String receiveStatus;
    private Integer printCount;
    private String remark;
}
