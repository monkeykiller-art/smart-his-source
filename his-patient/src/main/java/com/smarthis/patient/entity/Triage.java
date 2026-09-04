package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_triage")
public class Triage extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long regId;

    private Long patientId;

    private Long deptId;

    private Long doctorId;

    private Integer visitSeq;

    private String triageStatus;

    private Integer queueNo;

    private LocalDateTime enqueueTime;

    private LocalDateTime callTime;

    private LocalDateTime finishTime;
}
