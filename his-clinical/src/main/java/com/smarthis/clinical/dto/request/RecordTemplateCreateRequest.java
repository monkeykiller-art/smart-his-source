package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecordTemplateCreateRequest {

    @NotBlank(message = "template name is required")
    private String templateName;

    private String templateType;

    private Long deptId;

    private String diseaseCode;

    private String recordType;

    private String templateContent;

    private Integer sortOrder;

    private Integer templateStatus;
}
