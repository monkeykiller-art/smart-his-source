package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pat_patient_merge_log")
public class PatientMergeLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long masterPatientId;

    private Long mergedPatientId;

    private String mergeReason;

    private String mergedBy;

    private LocalDateTime mergedTime;
}
