package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_order")
public class Order extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long deptId;

    private Long doctorId;

    private String orderType;

    private String orderCategory;

    private String orderStatus;

    private Integer priority;

    private Integer isStat;

    private Integer isPrn;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime orderTime;

    private Long verifyNurseId;

    private LocalDateTime verifyTime;

    private Long cancelNurseId;

    private LocalDateTime cancelTime;

    private String cancelReason;

    private Long executeDeptId;

    private String remark;

    private String groupNo;

    private Long billId;
}
