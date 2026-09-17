package com.smarthis.pharma.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CdssAlertConfigQueryRequest extends PageQuery {

    private String alertType;

    private String alertLevel;

    private Integer isEnabled;

    private Long deptId;
}
