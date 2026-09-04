package com.smarthis.pharma.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoseLimitVo {

    private Long id;

    private String drugCode;

    private String patientType;

    private Integer ageMin;

    private Integer ageMax;

    private String route;

    private BigDecimal maxSingleDose;

    private String maxSingleUnit;

    private BigDecimal maxDailyDose;

    private String maxDailyUnit;

    private Integer maxFreqPerDay;

    private String description;

    private Integer isActive;
}
