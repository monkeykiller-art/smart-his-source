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
@TableName("res_stock_movement")
public class StockMovement extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String movementNo;

    private Long drugId;

    private Long pharmacyId;

    private String batchNo;

    private String movementType;

    private BigDecimal quantity;

    private BigDecimal unitCost;

    private BigDecimal totalAmount;

    private BigDecimal beforeQty;

    private BigDecimal afterQty;

    private String referenceType;

    private Long referenceId;

    private Long operatorId;

    private LocalDateTime movementTime;

    private String remark;
}
