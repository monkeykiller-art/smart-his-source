package com.smarthis.operations.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import com.smarthis.operations.enums.PayMethod;
import com.smarthis.operations.enums.SettleType;
import com.smarthis.operations.enums.VisitType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_settlement")
public class Settlement extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String settleNo;

    private Long patientId;

    private Long admissionId;

    private Long encounterId;

    private VisitType visitType;

    private String patientType;

    private String invoiceNo;

    private BigDecimal totalAmount;

    private BigDecimal insuranceAmount;

    private BigDecimal depositAmount;

    private BigDecimal selfPayAmount;

    private BigDecimal settleBalance;

    private SettleType settleType;

    private PayMethod payMethod;

    private String cashierId;

    private String cashierName;

    private LocalDateTime settleTime;

    private String settleStatus;

    private LocalDateTime cancelTime;

    private String cancelBy;

    private String cancelReason;

    private String remark;
}
