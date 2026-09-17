package com.smarthis.emergency.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class ResuscitationCreateRequest {
    @NotNull private Long triageId;
    @NotNull private Long patientId;
    private String procedures;
    private String medications;
}
