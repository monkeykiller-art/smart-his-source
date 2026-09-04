package com.smarthis.clinical.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RecordTemplateQueryRequest extends PageQuery {

    private String templateName;

    private String templateType;

    private Long deptId;

    private String recordType;

    private Integer templateStatus;
}
