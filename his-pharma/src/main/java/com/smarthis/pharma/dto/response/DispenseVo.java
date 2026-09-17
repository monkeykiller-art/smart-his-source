package com.smarthis.pharma.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DispenseVo {
    private Long id;
    private String dispenseNo;
    private Long prescriptionId;
    private Long rxReviewId;
    private Long patientId;
    private String warehouseCode;
    private String dispenseStatus;
    private Long pharmacistId;
    private String pharmacistName;
    private LocalDateTime dispenseTime;
    private LocalDateTime returnTime;
    private String remark;
    private List<DispenseItemVo> items;
}
