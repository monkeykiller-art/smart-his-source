package com.smarthis.emergency.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class ObservationCreateRequest {
    @NotNull private Long triageId;
    @NotNull private Long patientId;
    @NotBlank private String bedNo;
    @NotBlank private String diagnosis;
    private String treatmentPlan;
}
