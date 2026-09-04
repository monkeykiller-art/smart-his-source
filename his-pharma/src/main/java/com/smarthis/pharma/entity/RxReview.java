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
@TableName("pha_rx_review")
public class RxReview extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String reviewNo;

    private Long orderId;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long doctorId;

    private Long deptId;

    private String prescriptionType;

    private String reviewStatus;

    private String reviewResult;

    private String reviewerId;

    private String reviewerName;

    private LocalDateTime reviewTime;

    private String rejectReason;

    private Integer warningCount;

    private Integer errorCount;

    private String remark;
}
