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
@TableName("ops_bill_transaction")
public class BillTransaction extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long billId;

    private String transactionNo;

    private String transactionType;

    private BigDecimal amount;

    private PayMethod payMethod;

    private String referenceNo;

    private String idempotencyKey;

    private String reason;

    private LocalDateTime transactionTime;

    private String transactionStatus;
}
