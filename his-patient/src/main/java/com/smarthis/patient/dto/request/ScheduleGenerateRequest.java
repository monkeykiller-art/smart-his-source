package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleGenerateRequest {

    @NotNull(message = "start date is required")
    private LocalDate startDate;

    @NotNull(message = "end date is required")
    private LocalDate endDate;

    private Long deptId;

    private Long doctorId;
}
