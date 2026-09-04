package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WardCreateRequest {
    @NotBlank private String wardCode;
    @NotBlank private String wardName;
    @NotNull private Long deptId;
    private String wardType;
    private String floorLocation;
    private Integer bedCount;
    private String nurseStation;
    private Long headNurseId;
    private String description;
}
