package com.smarthis.clinical.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkinTestQueryRequest extends PageQuery {

    private Long patientId;

    private Long admissionId;

    private String testResult;
}
