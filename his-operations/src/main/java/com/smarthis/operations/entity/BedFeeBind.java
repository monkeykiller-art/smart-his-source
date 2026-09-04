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
@TableName("ops_bed_fee_bind")
public class BedFeeBind extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long bedId;

    private Long feeItemId;

    private String feeItemCode;

    private String feeItemName;

    private BigDecimal dailyFee;

    private Integer isActive;
}
