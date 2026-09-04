package com.smarthis.clinical.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiagnosisQueryRequest extends PageQuery {

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private String diagnosisType;

    private String diagnosisStatus;
}
