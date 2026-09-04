package com.smarthis.patient.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionQueryRequest extends PageQuery {

    private Long patientId;

    private Long deptId;

    private Long wardId;

    private String admissionStatus;

    private String admissionType;

    private LocalDate admissionDateFrom;

    private LocalDate admissionDateTo;

    private String keyword;
}
