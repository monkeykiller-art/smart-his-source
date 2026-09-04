package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DrugUsageDictCreateRequest {

    @NotBlank(message = "usage code is required")
    private String usageCode;

    @NotBlank(message = "usage name is required")
    private String usageName;

    private String namePinyin;

    private String usageDesc;

    private Integer isInjection;

    private Integer needSkinTest;

    private Integer sortOrder;

    private Integer dictStatus;
}
