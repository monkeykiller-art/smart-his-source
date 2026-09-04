package com.smarthis.pharma.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pha_drug_allergy_cross")
public class DrugAllergyCross extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String allergyCode;

    private String allergyName;

    private String crossDrugCode;

    private String crossDrugName;

    private String crossLevel;

    private String description;

    private Integer isActive;
}
