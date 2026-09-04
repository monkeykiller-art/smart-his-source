package com.smarthis.operations.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class FeeTemplateVo {
    private Long id;
    private String templateName;
    private String templateCategory;
    private String templateLevel;
    private Long deptId;
    private String levelType;
    private Integer sortOrder;
    private Integer templateStatus;
    private List<FeeTemplateItemVo> items;
}
