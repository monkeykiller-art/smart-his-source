package com.smarthis.clinical.dto.response;

import lombok.Data;

@Data
public class RecordTemplateVo {

    private Long id;

    private String templateName;

    private String templateType;

    private Long deptId;

    private String diseaseCode;

    private String recordType;

    private String templateContent;

    private Integer sortOrder;

    private Integer templateStatus;
}
