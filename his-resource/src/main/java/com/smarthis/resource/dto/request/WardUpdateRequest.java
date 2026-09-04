package com.smarthis.resource.dto.request;

import lombok.Data;

@Data
public class WardUpdateRequest {
    private String wardName;
    private String wardType;
    private String floorLocation;
    private Integer bedCount;
    private String nurseStation;
    private Long headNurseId;
    private String wardStatus;
    private String description;
}
