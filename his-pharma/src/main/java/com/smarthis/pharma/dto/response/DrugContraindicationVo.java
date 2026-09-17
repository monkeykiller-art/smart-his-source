package com.smarthis.pharma.dto.response;

import lombok.Data;

@Data
public class DrugContraindicationVo {

    private Long id;

    private String drugCode;

    private String contraindicationType;

    private String contraindicationCode;

    private String contraindicationName;

    private String severityLevel;

    private String description;

    private String suggestion;

    private Integer isActive;
}
