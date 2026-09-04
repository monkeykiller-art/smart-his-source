package com.smarthis.resource.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PharmacyVo {
    private Long id;
    private String pharmacyCode;
    private String pharmacyName;
    private String pharmacyType;
    private Long deptId;
    private String location;
    private String phone;
    private String pharmacyStatus;
    private LocalDateTime createdTime;
}
