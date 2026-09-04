package com.smarthis.clinical.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderTemplateQueryRequest extends PageQuery {

    private String templateName;

    private String templateLevel;

    private Long deptId;

    private String templateCategory;

    private String orderType;

    private Integer templateStatus;
}
