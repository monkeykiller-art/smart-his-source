package com.smarthis.operations.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import com.smarthis.operations.enums.AccountStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_account")
public class Account extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String accountNo;

    private String cashierId;

    private String cashierName;

    private String settleType;

    private BigDecimal totalAmount;

    private BigDecimal cashAmount;

    private BigDecimal posAmount;

    private BigDecimal otherAmount;

    private Integer billCount;

    private LocalDate accountDate;

    private LocalDateTime submitTime;

    private LocalDateTime receiveTime;

    private String receiverId;

    private AccountStatus accountStatus;

    private String receiveStatus;

    private Integer printCount;

    private String remark;
}
