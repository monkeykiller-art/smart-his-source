package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_order_item")
public class OrderItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    private Integer itemSeq;

    private String itemCode;

    private String itemName;

    private String itemType;

    private String spec;

    private BigDecimal dose;

    private String doseUnit;

    private String usageMethod;

    private String frequency;

    private Integer days;

    private BigDecimal quantity;

    private String quantityUnit;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private Integer isFirstDay;

    private String dripRate;

    private String skinTestResult;

    private String remark;

    private String itemStatus;
}
