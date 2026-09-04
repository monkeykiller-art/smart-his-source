package com.smarthis.pharma.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RxReviewQueryRequest extends PageQuery {

    private String reviewStatus;

    private Long doctorId;

    private Long patientId;

    private String prescriptionType;
}
