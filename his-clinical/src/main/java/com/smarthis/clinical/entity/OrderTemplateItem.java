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
@TableName("cli_order_template_item")
public class OrderTemplateItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;

    private Integer groupNo;

    private String itemCode;

    private String itemName;

    private String spec;

    private BigDecimal dose;

    private String doseUnit;

    private String usageMethod;

    private String frequency;

    private Integer isFirstDay;

    private BigDecimal quantity;

    private String quantityUnit;

    private String dripRate;

    private Long executeDeptId;

    private String doctorAdvice;

    private String orderCategory;

    private Integer itemSeq;
}
