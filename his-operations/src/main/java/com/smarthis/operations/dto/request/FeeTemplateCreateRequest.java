package com.smarthis.operations.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class FeeTemplateCreateRequest {

    @NotBlank(message = "template name is required")
    private String templateName;

    private String templateCategory;

    private String templateLevel;

    private Long deptId;

    private String levelType;

    private Integer sortOrder;

    private List<FeeTemplateItemRequest> items;
}
