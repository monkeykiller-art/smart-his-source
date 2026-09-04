package com.smarthis.operations.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FeeItemVo {
    private Long id;
    private String itemCode;
    private String itemName;
    private String namePinyin;
    private String itemClass;
    private String itemCategory;
    private String spec;
    private String unit;
    private BigDecimal unitPrice;
    private String dosageForm;
    private Integer isInsurance;
    private BigDecimal insuranceRatio;
    private Integer isSelfPay;
    private String executeDeptType;
    private Integer needConfirm;
    private Integer sortOrder;
    private Integer itemStatus;
}
