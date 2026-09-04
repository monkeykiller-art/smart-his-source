package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalRecordCreateRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    private Long encounterId;

    private Long admissionId;

    @NotNull(message = "dept id is required")
    private Long deptId;

    @NotNull(message = "doctor id is required")
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
}
