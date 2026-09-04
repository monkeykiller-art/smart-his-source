package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderCreateRequest {

    @NotNull(message = "patient id is required")
    private Long patientId;

    private Long encounterId;

    private Long admissionId;

    @NotNull(message = "dept id is required")
    private Long deptId;

    @NotNull(message = "doctor id is required")
    private Long doctorId;

    @NotNull(message = "order type is required")
    private String orderType;

    private String orderCategory;

    private Integer priority;

    private Integer isStat;

    private Integer isPrn;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long executeDeptId;

    private String remark;

    private String groupNo;

    private List<OrderItemRequest> items;
}
