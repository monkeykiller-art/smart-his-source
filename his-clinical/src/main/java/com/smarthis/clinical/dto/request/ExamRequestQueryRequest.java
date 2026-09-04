package com.smarthis.clinical.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamRequestQueryRequest extends PageQuery {

    private Long patientId;

    private Long encounterId;

    private Long admissionId;

    private String requestType;

    private String requestStatus;
}
