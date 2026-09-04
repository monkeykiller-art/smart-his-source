package com.smarthis.patient.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RegistrationVo {

    private Long id;

    private String regNo;

    private Long patientId;

    private String patientName;

    private Long scheduleId;

    private Long deptId;

    private String deptName;

    private Long doctorId;

    private String doctorName;

    private Integer visitSeq;

    private LocalDate regDate;

    private String timePeriod;

    private BigDecimal regFee;

    private String payStatus;

    private LocalDateTime payTime;

    private String regSource;

    private String regStatus;

    private Long billId;

    private Long encounterId;

    private LocalDateTime createdTime;
}
