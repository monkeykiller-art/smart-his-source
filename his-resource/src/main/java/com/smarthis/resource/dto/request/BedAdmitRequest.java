package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BedAdmitRequest {
    @NotNull private Long patientId;
    @NotNull private Long admissionId;
    @NotNull private Long wardId;
    @NotNull private Long bedId;
    private Integer expectedStay;
}
