package com.smarthis.patient.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleVo {

    private Long id;

    private Long deptId;

    private String deptName;

    private Long doctorId;

    private String doctorName;

    private LocalDate scheduleDate;

    private String timePeriod;

    private String startTime;

    private String endTime;

    private Integer totalQuota;

    private Integer usedQuota;

    private Integer availableQuota;

    private BigDecimal regFee;

    private String regLevel;

    private String scheduleStatus;
}
