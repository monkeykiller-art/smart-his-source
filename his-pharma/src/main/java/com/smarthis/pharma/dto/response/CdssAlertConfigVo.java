package com.smarthis.pharma.dto.response;

import lombok.Data;

@Data
public class CdssAlertConfigVo {

    private Long id;

    private String alertType;

    private String alertName;

    private String alertLevel;

    private Integer isEnabled;

    private Long deptId;

    private String description;

    private Integer sortOrder;
}
