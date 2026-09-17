package com.smarthis.pharma.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DispenseCreateRequest {
    @NotNull
    private Long prescriptionId;
    @NotNull
    private Long rxReviewId;
    @NotNull
    private Long patientId;
    @NotBlank
    private String warehouseCode;
    private Long pharmacistId;
    private String pharmacistName;
    private String remark;
    @Valid @NotEmpty
    private List<DispenseItemRequest> items;
}
