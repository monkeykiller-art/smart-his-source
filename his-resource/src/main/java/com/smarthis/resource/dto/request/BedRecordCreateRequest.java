package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BedRecordCreateRequest {
    @NotNull private Long bedId;
    @NotNull private Long patientId;
    private Long admissionId;
    private Long wardId;
    private String bedNo;
    private Integer expectedStay;
}
