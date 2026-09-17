package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdmissionTransferRequest {
    @NotNull
    private Long targetDeptId;
    @NotNull
    private Long targetWardId;
    @NotNull
    private Long targetBedId;
    @NotBlank
    private String reason;
}
