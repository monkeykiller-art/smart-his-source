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
@TableName("pha_dose_limit")
public class DoseLimit extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String drugCode;

    private String patientType;

    private Integer ageMin;

    private Integer ageMax;

    private String route;

    private BigDecimal maxSingleDose;

    private String maxSingleUnit;

    private BigDecimal maxDailyDose;

    private String maxDailyUnit;

    private Integer maxFreqPerDay;

    private String description;

    private Integer isActive;
}
