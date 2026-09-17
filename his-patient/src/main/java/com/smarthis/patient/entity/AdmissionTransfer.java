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
@TableName("pat_admission_transfer")
public class AdmissionTransfer extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long admissionId;
    private Long fromDeptId;
    private Long fromWardId;
    private Long fromBedId;
    private Long toDeptId;
    private Long toWardId;
    private Long toBedId;
    private String transferReason;
    private LocalDateTime transferTime;
}
