package com.smarthis.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_drug_price")
public class DrugPrice extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long drugId;

    private Long pharmacyId;

    private BigDecimal price;

    private BigDecimal retailPrice;

    private Integer isActive;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
}
