package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillVo {
    private Long id;
    private String billNo;
    private Long patientId;
    private Long admissionId;
    private Long encounterId;
    private String visitType;
    private Long deptId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private BigDecimal paidAmount;
    private String billStatus;
    private String billType;
    private String remark;
    private LocalDateTime createdTime;
}
