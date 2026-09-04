package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SettlementPreviewVo {
    private Long patientId;
    private Long admissionId;
    private BigDecimal totalAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal depositAmount;
    private BigDecimal selfPayAmount;
    private BigDecimal depositBalance;
    private List<SettlementItemVo> items;
}
