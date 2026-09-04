package com.smarthis.operations.dto.request;

import lombok.Data;

@Data
public class SettlementCancelRequest {

    private String cancelBy;

    private String cancelReason;
}
