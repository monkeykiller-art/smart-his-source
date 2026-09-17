package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoseLimitCreateRequest {

    @NotBlank(message = "drug code is required")
    private String drugCode;

    private String patientType;

    private Integer ageMin;

    private Integer ageMax;

    private String route;

    private BigDecimal maxSingleDose;

    private String maxSingleUnit;

    private BigDecimal maxDailyDose;

    private String maxDailyUnit;

    private Integer maxFreqPerDay;

    private String description;
}
