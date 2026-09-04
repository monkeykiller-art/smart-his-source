package com.smarthis.clinical.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ExamRequestCreateRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    private Long encounterId;

    private Long admissionId;

    @NotNull(message = "dept id is required")
    private Long deptId;

    @NotNull(message = "doctor id is required")
    private Long doctorId;

    @NotBlank(message = "request type is required")
    private String requestType;

    private Integer isUrgent;

    private String clinicalDiagnosis;

    private String clinicalInfo;

    private Long requestDeptId;

    private Long executeDeptId;

    private String remark;

    @NotEmpty(message = "exam request items are required")
    @Valid
    private List<ExamRequestItemRequest> items;
}
