package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettlementRequest {

    @NotNull(message = "admissionId is required")
    private Long admissionId;

    @NotNull(message = "patientId is required")
    private Long patientId;

    private Long encounterId;

    private String visitType;

    @NotNull(message = "settleType is required")
    private String settleType;

    @NotNull(message = "payMethod is required")
    private String payMethod;

    private String patientType;

    private String remark;
}
