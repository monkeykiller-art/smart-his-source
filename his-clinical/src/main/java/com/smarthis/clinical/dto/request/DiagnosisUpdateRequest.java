package com.smarthis.clinical.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisUpdateRequest {

    private Long icd10Id;

    private String icdCode;

    private String diagnosisName;

    private String diagnosisType;

    private Integer isPrimary;

    private Integer isConfirmed;

    private Integer diagnosisSeq;

    private LocalDate onsetDate;

    private String diagnosisDesc;

    private String diagnosisStatus;
}
