package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DispenseCreateRequest {
    @NotNull private Long patientId;
    private Long admissionId;
    private Long encounterId;
    @NotNull private Long pharmacyId;
    private Long orderId;
    private Long billId;
    @NotBlank private String dispenseType;
    private BigDecimal totalAmount;
    private String remark;
    private List<DispenseItemRequest> items;

    @Data
    public static class DispenseItemRequest {
        @NotNull private Long drugId;
        private String drugCode;
        private String drugName;
        private String spec;
        @NotNull private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private String batchNo;
        private String usageMethod;
        private String frequency;
        private Integer days;
        private String remark;
    }
}
