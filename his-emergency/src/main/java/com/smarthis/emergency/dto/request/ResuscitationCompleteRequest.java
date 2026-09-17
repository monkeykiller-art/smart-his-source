package com.smarthis.emergency.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class ResuscitationCompleteRequest {
    @NotBlank private String outcome;
    @NotBlank private String outcomeSummary;
}
