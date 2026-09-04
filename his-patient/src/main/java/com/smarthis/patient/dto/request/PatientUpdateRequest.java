package com.smarthis.patient.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientUpdateRequest {

    private String name;

    private Integer gender;

    private LocalDate birthDate;

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
}
