package com.smarthis.operations.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeeItemQueryRequest extends PageQuery {

    private String keyword;

    private String itemClass;
}
