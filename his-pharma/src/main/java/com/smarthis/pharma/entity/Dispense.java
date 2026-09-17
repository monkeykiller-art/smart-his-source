package com.smarthis.pharma.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pha_dispense")
public class Dispense extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String dispenseNo;
    private Long prescriptionId;
    private Long rxReviewId;
    private Long patientId;
    private String warehouseCode;
    private String dispenseStatus;
    private Long pharmacistId;
    private String pharmacistName;
    private LocalDateTime dispenseTime;
    private LocalDateTime returnTime;
    private String remark;
}
