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
@TableName("res_bed")
public class Bed extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String bedNo;

    private Long wardId;

    private String roomNo;

    private String bedType;

    private String bedRank;

    private Integer floorNo;

    private String bedStatus;

    private Integer isMale;

    private Long feeItemId;

    private BigDecimal dailyFee;

    private Integer sortOrder;

    private String remark;
}
