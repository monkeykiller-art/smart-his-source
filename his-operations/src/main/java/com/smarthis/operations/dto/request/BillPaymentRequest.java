package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillPaymentRequest {

    @NotNull(message = "payment amount is required")
    @DecimalMin(value = "0.0001", message = "payment amount must be positive")
    @Digits(integer = 14, fraction = 4, message = "payment amount supports up to four decimal places")
    private BigDecimal amount;

    @NotBlank(message = "payment method is required")
    @Pattern(regexp = "CASH|POS|WECHAT|ALIPAY", message = "unsupported payment method")
    private String payMethod;

    @Size(max = 64)
    private String referenceNo;

    @NotBlank(message = "idempotency key is required")
    @Size(min = 8, max = 64)
    private String idempotencyKey;
}
