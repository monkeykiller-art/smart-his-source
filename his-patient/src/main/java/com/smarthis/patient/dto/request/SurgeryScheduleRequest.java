package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SurgeryScheduleRequest {
    @NotNull @FutureOrPresent private LocalDateTime plannedStartTime;
    @NotBlank private String operatingRoom;
    @NotNull private Long anesthetistId;
    @NotBlank private String anesthesiaMethod;
}
