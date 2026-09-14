package com.smarthis.operations.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import com.smarthis.operations.enums.BillStatus;
import com.smarthis.operations.enums.BillType;
import com.smarthis.operations.enums.VisitType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops_bill")
public class Bill extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String billNo;

    private Long patientId;

    private Long admissionId;

    private Long encounterId;

    private VisitType visitType;

    private Long deptId;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payableAmount;

    private BigDecimal paidAmount;

    private BillStatus billStatus;

    private BillType billType;

    private String remark;

    private String voidReason;
}
