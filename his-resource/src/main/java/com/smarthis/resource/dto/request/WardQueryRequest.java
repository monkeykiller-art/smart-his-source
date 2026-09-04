package com.smarthis.resource.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WardQueryRequest extends PageQuery {
    private String wardName;
    private Long deptId;
    private String wardType;
    private String wardStatus;
}
