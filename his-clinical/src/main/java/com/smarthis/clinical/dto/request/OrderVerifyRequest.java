package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderVerifyRequest {

    @NotNull(message = "nurse id is required")
    private Long nurseId;

    private String remark;
}
