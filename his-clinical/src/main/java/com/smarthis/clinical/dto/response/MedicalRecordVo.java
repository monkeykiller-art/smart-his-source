package com.smarthis.clinical.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MedicalRecordVo {

    private Long id;

    private String recordNo;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long deptId;

    private Long doctorId;

    private String recordType;

    private Long templateId;

    private String title;

    private String chiefComplaint;

    private String presentIllness;

    private String pastHistory;

    private String allergyHistory;

    private String physicalExam;

    private String auxiliaryExam;

    private String diagnosisDesc;

    private String treatmentPlan;

    private String recordContent;

    private String recordStatus;

    private LocalDateTime signTime;

    private BigDecimal qualityScore;

    private String qualityResult;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
