package com.smarthis.operations.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_fee_item")
public class FeeItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
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
