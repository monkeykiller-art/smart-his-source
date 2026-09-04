package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FeeTemplateItemVo {
    private Long id;
    private Long templateId;
    private String itemType;
    private String itemCode;
    private String itemName;
    private BigDecimal quantity;
    private String unit;
    private Long executeDeptId;
    private Integer itemSeq;
}
