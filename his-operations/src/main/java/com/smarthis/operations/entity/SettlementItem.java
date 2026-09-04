package com.smarthis.operations.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_settlement_item")
public class SettlementItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long settlementId;

    private String itemClass;

    private Integer itemCount;

    private BigDecimal totalAmount;

    private BigDecimal insuranceAmount;

    private BigDecimal selfPayAmount;
}
