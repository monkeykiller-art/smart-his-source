package com.smarthis.resource.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BedDischargeRequest {
    private LocalDateTime dischargeTime;
}
