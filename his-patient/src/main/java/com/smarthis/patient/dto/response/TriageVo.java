package com.smarthis.patient.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TriageVo {

    private Long id;

    private Long regId;

    private String regNo;

    private Long patientId;

    private String patientName;

    private Long deptId;

    private String deptName;

    private Long doctorId;

    private String doctorName;

    private Integer visitSeq;

    private String triageStatus;

    private Integer queueNo;

    private LocalDateTime enqueueTime;

    private LocalDateTime callTime;

    private LocalDateTime finishTime;
}
