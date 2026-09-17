package com.smarthis.pharma.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pha_drug_interaction")
public class DrugInteraction extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String drugCodeA;

    private String drugCodeB;

    private String interactionLevel;

    private String interactionDesc;

    private String suggestion;

    private String reference;

    private Integer isActive;
}
