package com.smarthis.emergency.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmergencyTriageCreateRequest {
    @NotNull private Long patientId;
    @NotNull @Min(1) @Max(4) private Integer triageLevel;
    @NotBlank private String chiefComplaint;
    private String vitalSigns;
    @NotNull private Long triageNurseId;
    private String triageNurseName;
    private Long targetDeptId;
    private String targetDeptName;
}
