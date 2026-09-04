package com.smarthis.pharma.dto.request;

import lombok.Data;

@Data
public class CdssConfigUpdateRequest {

    private String alertName;

    private String alertLevel;

    private Integer isEnabled;

    private String description;

    private Integer sortOrder;
}
