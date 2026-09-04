package com.smarthis.resource.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BedRecordVo {
    private Long id;
    private Long bedId;
    private Long patientId;
    private Long admissionId;
    private Long wardId;
    private String bedNo;
    private LocalDateTime admitTime;
    private LocalDateTime dischargeTime;
    private Integer expectedStay;
    private String recordStatus;
    private LocalDateTime createdTime;
}
