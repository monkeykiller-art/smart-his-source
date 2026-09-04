package com.smarthis.resource.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WardVo {
    private Long id;
    private String wardCode;
    private String wardName;
    private Long deptId;
    private String wardType;
    private String floorLocation;
    private Integer bedCount;
    private String nurseStation;
    private Long headNurseId;
    private String wardStatus;
    private String description;
    private LocalDateTime createdTime;
}
