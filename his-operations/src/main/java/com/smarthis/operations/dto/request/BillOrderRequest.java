package com.smarthis.operations.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BillOrderRequest {
    @NotNull @Positive private Long orderId;
    @NotNull @Positive private Long patientId;
    @NotNull @Positive private Long encounterId;
    @NotNull @Positive private Long deptId;
    @NotEmpty @Valid private List<Item> items;

    @Data
    public static class Item {
        @NotNull @Positive private Long orderItemId;
        private String itemCode;
        @NotBlank @Size(max = 128) private String itemName;
        private String itemClass;
        private String spec;
        private String unit;
        @NotNull @DecimalMin("0") @Digits(integer = 14, fraction = 4)
        private BigDecimal unitPrice;
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 14, fraction = 4)
        private BigDecimal quantity;
    }
}
