package com.smarthis.clinical.dto.request;

import lombok.Data;

@Data
public class OrderCancelRequest {

    private Long nurseId;

    private String reason;
}
