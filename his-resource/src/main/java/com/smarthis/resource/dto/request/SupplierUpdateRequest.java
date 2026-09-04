package com.smarthis.resource.dto.request;

import lombok.Data;

@Data
public class SupplierUpdateRequest {
    private String supplierName;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private String licenseNo;
    private String supplierType;
    private String supplierStatus;
}
