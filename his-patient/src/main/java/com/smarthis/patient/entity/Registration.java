package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_registration")
public class Registration extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String regNo;

    private Long patientId;

    private Long scheduleId;

    private Long deptId;

    private Long doctorId;

    private Integer visitSeq;

    private LocalDate regDate;

    private String timePeriod;

    private BigDecimal regFee;

    private String payStatus;

    private LocalDateTime payTime;

    private String regSource;

    private String regStatus;

    private Long billId;

    private String cancelReason;

    private LocalDateTime cancelTime;
}
