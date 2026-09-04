package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettlementCreateRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    @NotNull(message = "admission id is required")
    private Long admissionId;

    private Long encounterId;

    private String visitType;

    private String patientType;

    private String settleType;

    private String payMethod;

    private String cashierId;

    private String cashierName;

    private String remark;
}
