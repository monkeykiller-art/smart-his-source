package com.smarthis.resource.dto.request;

import lombok.Data;

@Data
public class PharmacyUpdateRequest {
    private String pharmacyName;
    private String pharmacyType;
    private String location;
    private String phone;
    private String pharmacyStatus;
}
