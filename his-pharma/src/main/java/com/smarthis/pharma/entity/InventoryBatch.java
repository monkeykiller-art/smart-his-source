package com.smarthis.pharma.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pha_inventory_batch")
public class InventoryBatch extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long drugId;
    private String warehouseCode;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private BigDecimal unitCost;
    private BigDecimal quantity;
    private BigDecimal availableQuantity;
    private BigDecimal lockedQuantity;
    private Integer isActive;
    @Version
    private Integer version;
}
