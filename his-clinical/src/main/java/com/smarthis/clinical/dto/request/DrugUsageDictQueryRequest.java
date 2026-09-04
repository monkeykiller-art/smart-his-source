package com.smarthis.clinical.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DrugUsageDictQueryRequest extends PageQuery {

    private String keyword;

    private Integer isInjection;

    private Integer dictStatus;
}
