package com.smarthis.patient.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentVo {

    private Long id;

    private String apptNo;

    private Long patientId;

    private String patientName;

    private Long scheduleId;

    private Long deptId;

    private String deptName;

    private Long doctorId;

    private String doctorName;

    private LocalDate apptDate;

    private String timePeriod;

    private String apptSource;

    private String apptStatus;

    private LocalDateTime confirmTime;

    private Long regId;
}
