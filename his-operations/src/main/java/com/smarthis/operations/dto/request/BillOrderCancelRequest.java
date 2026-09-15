package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BillOrderCancelRequest {
    @NotNull @Positive private Long orderId;
    @NotNull @Positive private Long patientId;
    @NotNull @Positive private Long encounterId;
    @NotNull @Positive private Long deptId;
    private Long billId;
    @NotBlank @Size(max = 256) private String reason;
}
