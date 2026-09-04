package com.smarthis.clinical.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class OrderTemplateCreateRequest {

    @NotBlank(message = "template name is required")
    private String templateName;

    private String templateLevel;

    private Long deptId;

    private String templateCategory;

    private String levelType;

    private String orderType;

    private Integer sortOrder;

    private Integer templateStatus;

    private List<OrderTemplateItemRequest> items;
}
