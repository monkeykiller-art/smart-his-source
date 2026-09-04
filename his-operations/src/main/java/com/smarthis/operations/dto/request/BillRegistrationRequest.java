package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillRegistrationRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    private Long encounterId;

    private String visitType;

    private Long deptId;

    private String billType;

    private String remark;
}
