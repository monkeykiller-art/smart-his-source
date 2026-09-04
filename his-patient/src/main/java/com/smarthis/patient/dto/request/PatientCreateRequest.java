package com.smarthis.patient.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientCreateRequest {

    @NotBlank(message = "name is required")
    private String name;

    private Integer gender;

    private LocalDate birthDate;

    @NotBlank(message = "id type is required")
    private String idType;

    @NotBlank(message = "id no is required")
    private String idNo;

    private String nationality;

    private String nation;

    private String maritalStatus;

    private String occupation;

    @NotBlank(message = "phone is required")
    private String phone;

    private String phoneBackup;

    private String address;

    private String bloodType;

    private String allergyHistory;

    private String insuranceType;

    private String insuranceNo;
}
