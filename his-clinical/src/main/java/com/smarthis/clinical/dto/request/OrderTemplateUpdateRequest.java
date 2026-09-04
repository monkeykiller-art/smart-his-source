package com.smarthis.clinical.dto.request;

import lombok.Data;

@Data
public class OrderTemplateUpdateRequest {

    private String templateName;

    private String templateLevel;

    private Long deptId;

    private String templateCategory;

    private String levelType;

    private String orderType;

    private Integer sortOrder;

    private Integer templateStatus;
}
