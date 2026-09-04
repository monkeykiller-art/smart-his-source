package com.smarthis.patient.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AdmissionDischargeRequest {

    private String dischargeType;

    private String dischargeSummary;

    private LocalDate actualDischargeDate;
}
