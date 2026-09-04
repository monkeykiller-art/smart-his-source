package com.smarthis.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PharmacyCreateRequest {
    @NotBlank private String pharmacyCode;
    @NotBlank private String pharmacyName;
    private String pharmacyType;
    private Long deptId;
    private String location;
    private String phone;
}
