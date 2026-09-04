package com.smarthis.clinical.dto.request;

import lombok.Data;

@Data
public class MedicalRecordUpdateRequest {

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
