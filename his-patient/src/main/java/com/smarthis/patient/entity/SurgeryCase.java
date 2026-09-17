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
@TableName("pat_surgery_case")
public class SurgeryCase extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String surgeryNo;
    private Long admissionId;
    private Long patientId;
    private String surgeryName;
    private LocalDateTime plannedStartTime;
    private String operatingRoom;
    private Long surgeonId;
    private Long anesthetistId;
    private String anesthesiaMethod;
    private String surgeryStatus;
    private String operativeNote;
}
