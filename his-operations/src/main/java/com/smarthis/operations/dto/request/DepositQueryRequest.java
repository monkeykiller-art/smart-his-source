package com.smarthis.operations.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DepositQueryRequest extends PageQuery {

    private Long patientId;

    private Long admissionId;

    private String depositStatus;
}
