package com.smarthis.pharma.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoseLimitQueryRequest extends PageQuery {

    private String drugCode;

    private String patientType;
}
