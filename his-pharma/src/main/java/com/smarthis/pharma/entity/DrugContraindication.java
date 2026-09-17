package com.smarthis.pharma.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pha_drug_contraindication")
public class DrugContraindication extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String drugCode;

    private String contraindicationType;

    private String contraindicationCode;

    private String contraindicationName;

    private String severityLevel;

    private String description;

    private String suggestion;

    private Integer isActive;
}
