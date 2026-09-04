package com.smarthis.clinical.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryRequest extends PageQuery {

    private Long patientId;

    private Long encounterId;

    private Long admissionId;

    private String orderType;

    private String orderCategory;

    private String orderStatus;
}
