package com.smarthis.patient.dto.request;

import com.smarthis.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegistrationQueryRequest extends PageQuery {

    private Long patientId;

    private Long doctorId;

    private Long deptId;

    private LocalDate regDate;

    private String payStatus;

    private String regStatus;
}
