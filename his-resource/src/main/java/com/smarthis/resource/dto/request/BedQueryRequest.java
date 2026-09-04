package com.smarthis.resource.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BedQueryRequest extends PageQuery {
    private Long wardId;
    private String bedStatus;
    private String bedType;
    private String bedRank;
}
