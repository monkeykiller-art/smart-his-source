package com.smarthis.clinical.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class OrderTemplateVo {

    private Long id;

    private String templateName;

    private String templateLevel;

    private Long deptId;

    private String templateCategory;

    private String levelType;

    private String orderType;

    private Integer sortOrder;

    private Integer templateStatus;

    private List<OrderTemplateItemVo> items;
}
