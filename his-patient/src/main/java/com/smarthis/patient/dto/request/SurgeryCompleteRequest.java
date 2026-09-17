package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SurgeryCompleteRequest {
    @NotBlank private String operativeNote;
}
