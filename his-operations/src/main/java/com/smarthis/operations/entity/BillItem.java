package com.smarthis.operations.entity;

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
@TableName("ops_bill_item")
public class BillItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long billId;

    private Integer itemSeq;

    private Long feeItemId;

    private String itemCode;

    private String itemName;

    private String itemClass;

    private String spec;

    private String unit;

    private BigDecimal unitPrice;

    private BigDecimal quantity;

    private BigDecimal amount;

    private Long chargeDeptId;

    private Long executeDeptId;

    private Long orderId;

    private Long orderItemId;

    private LocalDateTime prescTime;

    private LocalDateTime chargeTime;

    private String chargerId;

    private Integer isRefunded;

    private LocalDateTime refundTime;

    private String refundBy;

    private String refundReason;

    private String itemStatus;

    private String remark;
}
