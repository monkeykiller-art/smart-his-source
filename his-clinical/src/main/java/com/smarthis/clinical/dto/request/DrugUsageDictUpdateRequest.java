package com.smarthis.clinical.dto.request;

import lombok.Data;

@Data
public class DrugUsageDictUpdateRequest {

    private String usageCode;

    private String usageName;

    private String namePinyin;

    private String usageDesc;

    private Integer isInjection;

    private Integer needSkinTest;

    private Integer sortOrder;

    private Integer dictStatus;
}
