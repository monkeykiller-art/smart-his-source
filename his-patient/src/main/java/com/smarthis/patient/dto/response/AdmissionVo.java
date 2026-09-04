package com.smarthis.patient.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdmissionVo {

    private Long id;
    private String admissionNo;
    private Long patientId;
    private String patientName;
    private Long encounterId;
    private String admissionType;
    private String admissionStatus;
    private Long deptId;
    private String deptName;
    private Long doctorId;
    private String doctorName;
    private Long wardId;
    private String wardName;
    private Long bedId;
    private String bedNo;
    private String insuranceType;
    private String insuranceNo;
    private String insuranceOrg;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactAddr;
    private LocalDate admissionDate;
    private LocalDate expectedDischargeDate;
    private LocalDate actualDischargeDate;
    private String chiefComplaint;
    private String preliminaryDiagnosis;
    private BigDecimal depositAmount;
    private BigDecimal totalDeposit;
    private String dischargeType;
    private String dischargeSummary;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
