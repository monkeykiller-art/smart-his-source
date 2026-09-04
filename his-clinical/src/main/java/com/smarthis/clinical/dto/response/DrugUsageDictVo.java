package com.smarthis.clinical.dto.response;

import lombok.Data;

@Data
public class DrugUsageDictVo {

    private Long id;

    private String usageCode;

    private String usageName;

    private String namePinyin;

    private String usageDesc;

    private Integer isInjection;

    private Integer needSkinTest;

    private Integer sortOrder;

    private Integer dictStatus;
}
