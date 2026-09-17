package com.smarthis.operations.dto.response;

import java.math.BigDecimal;

public record AnalyticsSummary(
        long outpatientVisits,
        long billCount,
        BigDecimal billedAmount,
        BigDecimal receivedAmount,
        BigDecimal refundedAmount,
        BigDecimal inventoryQuantity,
        BigDecimal expiringInventoryQuantity) {}
