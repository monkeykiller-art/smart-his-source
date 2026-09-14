package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillRefundRequest {

    @NotNull(message = "refund amount is required")
    @DecimalMin(value = "0.0001", message = "refund amount must be positive")
    @Digits(integer = 14, fraction = 4, message = "refund amount supports up to four decimal places")
    private BigDecimal amount;

    @NotBlank(message = "refund reason is required")
    @Size(max = 256)
    private String reason;

    @NotBlank(message = "idempotency key is required")
    @Size(min = 8, max = 64)
    private String idempotencyKey;
}
