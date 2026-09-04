package com.smarthis.patient.dto.response;

import lombok.Data;

@Data
public class DoctorVo {

    private Long id;

    private String employeeNo;

    private String doctorName;

    private String namePinyin;

    private Integer gender;

    private Long deptId;

    private String deptName;

    private String title;

    private String specialty;

    private Integer prescribeRight;

    private Integer antibioticLevel;

    private String doctorStatus;
}
