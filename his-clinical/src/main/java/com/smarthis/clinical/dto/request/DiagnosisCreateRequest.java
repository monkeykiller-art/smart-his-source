package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisCreateRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    private Long encounterId;

    private Long admissionId;

    @NotNull(message = "doctor id is required")
    private Long doctorId;

    private Long icd10Id;

    private String icdCode;

    @NotNull(message = "diagnosis name is required")
    private String diagnosisName;

    private String diagnosisType;

    private Integer isPrimary;

    private Integer isConfirmed;

    private Integer diagnosisSeq;

    private LocalDate onsetDate;

    private String diagnosisDesc;
}
