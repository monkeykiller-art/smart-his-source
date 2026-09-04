package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DrugContraindicationCreateRequest {

    @NotBlank(message = "drug code is required")
    private String drugCode;

    @NotBlank(message = "contraindication type is required")
    private String contraindicationType;

    private String contraindicationCode;

    @NotBlank(message = "contraindication name is required")
    private String contraindicationName;

    private String severityLevel;

    @NotBlank(message = "description is required")
    private String description;

    private String suggestion;
}
