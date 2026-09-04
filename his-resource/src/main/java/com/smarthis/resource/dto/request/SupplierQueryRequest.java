package com.smarthis.resource.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SupplierQueryRequest extends PageQuery {
    private String keyword;
    private String supplierType;
    private String supplierStatus;
}
