package com.smarthis.clinical.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisVo {

    private Long id;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long doctorId;

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
