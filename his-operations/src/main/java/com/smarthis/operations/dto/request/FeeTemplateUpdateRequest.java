package com.smarthis.operations.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class FeeTemplateUpdateRequest {

    private String templateName;

    private String templateCategory;

    private String templateLevel;

    private Long deptId;

    private String levelType;

    private Integer sortOrder;

    private Integer templateStatus;

    private List<FeeTemplateItemRequest> items;
}
