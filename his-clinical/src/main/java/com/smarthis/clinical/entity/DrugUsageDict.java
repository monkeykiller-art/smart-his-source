package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_drug_usage_dict")
public class DrugUsageDict extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String usageCode;

    private String usageName;

    private String namePinyin;

    private String usageDesc;

    private Integer isInjection;

    private Integer needSkinTest;

    private Integer sortOrder;

    private Integer dictStatus;
}
