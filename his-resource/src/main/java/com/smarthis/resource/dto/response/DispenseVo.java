package com.smarthis.resource.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DispenseVo {
    private Long id;
    private String dispenseNo;
    private Long patientId;
    private Long admissionId;
    private Long encounterId;
    private Long pharmacyId;
    private Long orderId;
    private Long billId;
    private String dispenseType;
    private String dispenseStatus;
    private BigDecimal totalAmount;
    private LocalDateTime dispenseTime;
    private Long dispenserId;
    private String dispenserName;
    private Long reviewerId;
    private LocalDateTime reviewTime;
    private String remark;
    private LocalDateTime createdTime;
    private List<DispenseItemVo> items;
}
