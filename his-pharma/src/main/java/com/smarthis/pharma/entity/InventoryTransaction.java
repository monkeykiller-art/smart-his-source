package com.smarthis.pharma.entity;

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
@TableName("pha_inventory_transaction")
public class InventoryTransaction extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String transactionNo;
    private String operationType;
    private Long drugId;
    private Long batchId;
    private String warehouseCode;
    private BigDecimal quantityChange;
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    private String referenceType;
    private Long referenceId;
    private Long operatorId;
    private String operatorName;
    private String reason;
    private LocalDateTime occurredTime;
}
