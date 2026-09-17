package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CdssAlertConfigCreateRequest {

    @NotBlank(message = "alert type is required")
    private String alertType;

    @NotBlank(message = "alert name is required")
    private String alertName;

    @NotBlank(message = "alert level is required")
    private String alertLevel;

    private Long deptId;

    private String description;

    private Integer sortOrder;
}
