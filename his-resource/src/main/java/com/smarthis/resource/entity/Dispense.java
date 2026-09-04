package com.smarthis.resource.entity;

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
@TableName("res_dispense")
public class Dispense extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String dispenseNo;

    private Long patientId;

    private Long admissionId;

    private Long encounterId;

    private Long pharmacyId;

    private Long orderId;

    private Long billId;

    private String dispenseType;

    private String dispenseStatus;

    private BigDecimal totalAmount;

    private LocalDateTime dispenseTime;

    private Long dispenserId;

    private String dispenserName;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String remark;
}
