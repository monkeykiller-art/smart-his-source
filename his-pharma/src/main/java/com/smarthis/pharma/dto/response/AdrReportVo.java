package com.smarthis.pharma.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdrReportVo {

    private Long id;

    private String reportNo;

    private Long patientId;

    private Long admissionId;

    private String drugCode;

    private String drugName;

    private String batchNo;

    private LocalDateTime adrOnsetTime;

    private String adrType;

    private String adrLevel;

    private String adrDesc;

    private String adrOutcome;

    private String reporterId;

    private String reporterName;

    private Long reportDeptId;

    private LocalDateTime reportTime;

    private String reportStatus;

    private String reviewComment;

    private String reviewerId;

    private LocalDateTime reviewTime;

    private Integer isReportedToAuthority;

    private String remark;
}
