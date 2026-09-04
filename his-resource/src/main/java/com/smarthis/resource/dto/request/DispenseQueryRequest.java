package com.smarthis.resource.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DispenseQueryRequest extends PageQuery {
    private Long patientId;
    private Long pharmacyId;
    private String dispenseStatus;
    private String dispenseType;
}
