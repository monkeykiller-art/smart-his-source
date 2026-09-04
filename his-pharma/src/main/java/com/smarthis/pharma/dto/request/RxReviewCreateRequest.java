package com.smarthis.pharma.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RxReviewCreateRequest {

    private Long orderId;

    private Long encounterId;

    private Long admissionId;

    @NotNull(message = "patient id is required")
    private Long patientId;

    @NotNull(message = "doctor id is required")
    private Long doctorId;

    @NotNull(message = "dept id is required")
    private Long deptId;

    private String prescriptionType;

    private String remark;

    private List<RxReviewItemRequest> items;
}
