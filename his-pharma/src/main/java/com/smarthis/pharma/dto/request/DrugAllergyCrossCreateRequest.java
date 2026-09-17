package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DrugAllergyCrossCreateRequest {

    @NotBlank(message = "allergy code is required")
    private String allergyCode;

    @NotBlank(message = "allergy name is required")
    private String allergyName;

    @NotBlank(message = "cross drug code is required")
    private String crossDrugCode;

    @NotBlank(message = "cross drug name is required")
    private String crossDrugName;

    private String crossLevel;

    private String description;
}
