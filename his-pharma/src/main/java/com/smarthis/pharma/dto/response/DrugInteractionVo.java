package com.smarthis.pharma.dto.response;

import lombok.Data;

@Data
public class DrugInteractionVo {

    private Long id;

    private String drugCodeA;

    private String drugCodeB;

    private String interactionLevel;

    private String interactionDesc;

    private String suggestion;

    private String reference;

    private Integer isActive;
}
