package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SkinTestCreateRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    private Long admissionId;

    private Long orderItemId;

    private String drugCode;

    private String drugName;

    private String batchNo;

    private LocalDateTime testTime;

    private Long testNurseId;

    private String remark;
}
