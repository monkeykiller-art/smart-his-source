package com.smarthis.pharma.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pha_drug_catalog")
public class DrugCatalog extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String drugCode;
    private String genericName;
    private String tradeName;
    private String pinyinCode;
    private String dosageForm;
    private String strength;
    private String manufacturer;
    private String approvalNo;
    private String packageUnit;
    private String minUnit;
    private BigDecimal conversionFactor;
    private BigDecimal purchasePrice;
    private BigDecimal retailPrice;
    private String prescriptionType;
    private String antibioticLevel;
    private Integer isActive;
}
