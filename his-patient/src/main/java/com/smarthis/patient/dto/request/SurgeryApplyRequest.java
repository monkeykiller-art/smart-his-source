package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SurgeryApplyRequest {
    @NotNull private Long admissionId;
    @NotBlank private String surgeryName;
    @NotNull @FutureOrPresent private LocalDateTime plannedStartTime;
    @NotNull private Long surgeonId;
}
