package com.smarthis.resource.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockQueryRequest extends PageQuery {
    private Long drugId;
    private Long pharmacyId;
    private String stockStatus;
    private String keyword;
}
