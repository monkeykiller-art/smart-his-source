package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AdmissionCreateRequest {

    @NotNull
    private Long patientId;

    private Long encounterId;

    private String admissionType;

    @NotNull
    private Long deptId;

    @NotNull
    private Long doctorId;

    private Long wardId;

    private Long bedId;

    private String insuranceType;

    private String insuranceNo;

    private String insuranceOrg;

    private String emergencyContactName;

    private String emergencyContactPhone;

    private String emergencyContactAddr;

    @NotNull
    private LocalDate admissionDate;

    private LocalDate expectedDischargeDate;

    private String chiefComplaint;

    private String preliminaryDiagnosis;

    private BigDecimal depositAmount;
}
