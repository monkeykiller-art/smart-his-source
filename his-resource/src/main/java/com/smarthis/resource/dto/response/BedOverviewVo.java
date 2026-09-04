package com.smarthis.resource.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BedOverviewVo {
    private Long bedId;
    private String bedNo;
    private String roomNo;
    private String bedType;
    private String bedRank;
    private Integer floorNo;
    private String bedStatus;
    private Integer isMale;
    private BigDecimal dailyFee;
    private Integer sortOrder;
    private Long currentPatientId;
    private String currentBedNo;
    private Long admissionId;
    private LocalDateTime admitTime;
    private Integer expectedStay;
    private String recordStatus;
}
