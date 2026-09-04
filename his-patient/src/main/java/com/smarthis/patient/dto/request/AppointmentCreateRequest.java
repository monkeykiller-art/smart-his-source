package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentCreateRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    @NotNull(message = "schedule id is required")
    private Long scheduleId;

    private String apptSource;
}
