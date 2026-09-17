package com.smarthis.pharma.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DrugCatalogQueryRequest extends PageQuery {
    private String keyword;
    private String dosageForm;
    private String prescriptionType;
    private Integer isActive;
}
