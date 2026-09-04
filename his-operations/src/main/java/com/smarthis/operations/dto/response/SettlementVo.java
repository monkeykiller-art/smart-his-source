package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SettlementVo {
    private Long id;
    private String settleNo;
    private Long patientId;
    private Long admissionId;
    private Long encounterId;
    private String visitType;
    private String patientType;
    private String invoiceNo;
    private BigDecimal totalAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal depositAmount;
    private BigDecimal selfPayAmount;
    private BigDecimal settleBalance;
    private String settleType;
    private String payMethod;
    private String cashierId;
    private String cashierName;
    private LocalDateTime settleTime;
    private String settleStatus;
    private String remark;
    private List<SettlementItemVo> items;
}
