package com.smarthis.operations.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import com.smarthis.operations.enums.DepositType;
import com.smarthis.operations.enums.PayMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_deposit")
public class Deposit extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String depositNo;

    private Long patientId;

    private Long admissionId;

    private String receiptNo;

    private BigDecimal amount;

    private PayMethod payMethod;

    private DepositType depositType;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String cashierId;

    private String cashierName;

    private LocalDateTime chargeTime;

    private String remark;

    private String depositStatus;
}
