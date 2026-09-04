package com.smarthis.patient.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientVo {

    private Long id;

    private String empiNo;

    private String name;

    private String namePinyin;

    private Integer gender;

    private LocalDate birthDate;

    private String ageDisplay;

    private String idType;

    private String idNo;

    private String nationality;

    private String nation;

    private String maritalStatus;

    private String occupation;

    private String phone;

    private String phoneBackup;

    private String address;

    private String bloodType;

    private String allergyHistory;

    private String insuranceType;

    private String insuranceNo;

    private String patientType;

    private String patientStatus;
}
