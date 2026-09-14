package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BillVoidRequest {

    @NotBlank(message = "void reason is required")
    @Size(max = 240)
    private String reason;
}
