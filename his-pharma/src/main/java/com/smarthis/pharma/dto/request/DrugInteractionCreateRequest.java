package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DrugInteractionCreateRequest {

    @NotBlank(message = "drug code A is required")
    private String drugCodeA;

    @NotBlank(message = "drug code B is required")
    private String drugCodeB;

    private String interactionLevel;

    @NotBlank(message = "interaction description is required")
    private String interactionDesc;

    private String suggestion;

    private String reference;
}
