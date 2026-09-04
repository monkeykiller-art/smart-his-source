package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BillChargeRequest {

    @NotNull(message = "bill id is required")
    private Long billId;

    @NotNull(message = "items are required")
    private List<ChargeItemDetail> items;

    @Data
    public static class ChargeItemDetail {

        @NotNull(message = "fee item id is required")
        private Long feeItemId;

        private String itemCode;

        private String itemName;

        private String itemClass;

        private String spec;

        private String unit;

        private BigDecimal unitPrice;

        @NotNull(message = "quantity is required")
        private BigDecimal quantity;

        private Long chargeDeptId;

        private Long executeDeptId;

        private Long orderId;

        private Long orderItemId;

        private String remark;
    }
}
