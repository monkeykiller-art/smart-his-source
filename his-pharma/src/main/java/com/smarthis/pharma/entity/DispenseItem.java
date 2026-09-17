package com.smarthis.pharma.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pha_dispense_item")
public class DispenseItem extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long dispenseId;
    private Long prescriptionItemId;
    private Long drugId;
    private Long batchId;
    private String batchNo;
    private LocalDate expiryDate;
    private BigDecimal quantity;
    private String unit;
}
