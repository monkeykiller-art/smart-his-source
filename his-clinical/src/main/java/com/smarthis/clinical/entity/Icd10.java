package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_icd10")
public class Icd10 extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String icdCode;

    private String icdName;

    private String namePinyin;

    private String chapter;

    private String block;

    private String category;

    private String subCategory;

    private Integer isInfectious;

    private Integer isChronic;

    private Integer isTcm;

    private Integer sortOrder;

    private Integer dictStatus;
}
