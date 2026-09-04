package com.smarthis.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_drug")
public class Drug extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String drugCode;

    private String drugName;

    private String namePinyin;

    private String genericName;

    private String dosageForm;

    private String spec;

    private String unit;

    private String packUnit;

    private BigDecimal packQty;

    private String manufacturer;

    private String approvalNo;

    private String barCode;

    private String drugType;

    private Integer isInsurance;

    private BigDecimal insuranceRatio;

    private Integer isNarcotic;

    private Integer isPsychotropic;

    private Integer isAntibiotic;

    private Integer antibioticLevel;

    private Integer needSkinTest;

    private BigDecimal maxSingleDose;

    private BigDecimal maxDailyDose;

    private String storageCondition;

    private Integer sortOrder;

    private String drugStatus;
}
