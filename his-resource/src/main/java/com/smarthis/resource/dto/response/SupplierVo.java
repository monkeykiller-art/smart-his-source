package com.smarthis.resource.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierVo {
    private Long id;
    private String supplierCode;
    private String supplierName;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private String licenseNo;
    private String supplierType;
    private String supplierStatus;
    private LocalDateTime createdTime;
}
