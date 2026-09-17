package com.smarthis.pharma.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdrReportQueryRequest extends PageQuery {

    private String reportStatus;

    private String drugCode;

    private Long patientId;
}
