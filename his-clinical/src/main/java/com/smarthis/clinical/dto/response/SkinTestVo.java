package com.smarthis.clinical.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SkinTestVo {

    private Long id;

    private Long patientId;

    private Long admissionId;

    private Long orderItemId;

    private String drugCode;

    private String drugName;

    private String batchNo;

    private String testResult;

    private LocalDateTime testTime;

    private Long testNurseId;

    private LocalDateTime judgeTime;

    private Long judgeNurseId;

    private String remark;

    private LocalDateTime createdTime;
}
