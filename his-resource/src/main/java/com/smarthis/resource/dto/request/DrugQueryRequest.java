package com.smarthis.resource.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DrugQueryRequest extends PageQuery {
    private String keyword;
    private String drugType;
    private Integer isInsurance;
    private Integer isNarcotic;
    private Integer isAntibiotic;
    private String drugStatus;
}
