package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TriageCallNextRequest {

    @NotNull(message = "dept id is required")
    private Long deptId;

    private Long doctorId;
}
