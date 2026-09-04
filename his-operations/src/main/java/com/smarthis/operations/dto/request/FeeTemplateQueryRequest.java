package com.smarthis.operations.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeeTemplateQueryRequest extends PageQuery {

    private String templateCategory;

    private String templateLevel;

    private Long deptId;
}
