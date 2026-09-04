package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierCreateRequest {
    @NotBlank private String supplierCode;
    @NotBlank private String supplierName;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private String licenseNo;
    private String supplierType;
}
