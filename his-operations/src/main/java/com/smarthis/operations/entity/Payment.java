package com.smarthis.operations.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import com.smarthis.operations.enums.PayMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_payment")
public class Payment extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long settlementId;

    private String paymentNo;

    private PayMethod payMethod;

    private BigDecimal payAmount;

    private String paySource;

    private String referenceNo;

    private LocalDateTime payTime;

    private String payStatus;
}
