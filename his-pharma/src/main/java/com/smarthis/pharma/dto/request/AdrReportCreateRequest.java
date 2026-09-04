package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdrReportCreateRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    private Long admissionId;

    private String drugCode;

    @NotBlank(message = "drug name is required")
    private String drugName;

    private String batchNo;

    private LocalDateTime adrOnsetTime;

    private String adrType;

    private String adrLevel;

    @NotBlank(message = "adr description is required")
    private String adrDesc;

    private String adrOutcome;

    @NotBlank(message = "reporter id is required")
    private String reporterId;

    private String reporterName;

    private Long reportDeptId;

    private String remark;
}
