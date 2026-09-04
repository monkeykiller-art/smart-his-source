package com.smarthis.resource.entity;

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
@TableName("res_stock")
public class Stock extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long drugId;

    private Long pharmacyId;

    private String batchNo;

    private BigDecimal quantity;

    private BigDecimal unitCost;

    private LocalDate produceDate;

    private LocalDate expiryDate;

    private Long supplierId;

    private String warehouseArea;

    private String stockStatus;
}
