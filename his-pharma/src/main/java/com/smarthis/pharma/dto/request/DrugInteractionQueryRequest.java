package com.smarthis.pharma.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DrugInteractionQueryRequest extends PageQuery {

    private String drugCodeA;

    private String drugCodeB;

    private String interactionLevel;
}
