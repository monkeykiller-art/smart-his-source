package com.smarthis.clinical.dto.request;

import lombok.Data;

@Data
public class RecordTemplateUpdateRequest {

    private String templateName;

    private String templateType;

    private Long deptId;

    private String diseaseCode;

    private String recordType;

    private String templateContent;

    private Integer sortOrder;

    private Integer templateStatus;
}
