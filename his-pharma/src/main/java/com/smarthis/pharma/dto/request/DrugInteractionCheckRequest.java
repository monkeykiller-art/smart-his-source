package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DrugInteractionCheckRequest {

    @NotBlank(message = "drug code A is required")
    private String drugCodeA;

    @NotBlank(message = "drug code B is required")
    private String drugCodeB;
}
