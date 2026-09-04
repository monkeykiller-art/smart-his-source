package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegistrationCreateRequest {

    private Long patientId;

    private String idType;

    private String idNo;

    private String name;

    private String phone;

    private Integer gender;

    private LocalDate birthDate;

    @NotNull(message = "schedule id is required")
    private Long scheduleId;

    private String regSource;
}
