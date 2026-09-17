package com.smarthis.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("emg_resuscitation")
public class ResuscitationRecord extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long triageId;
    private Long patientId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String procedures;
    private String medications;
    private String outcome;
    private String outcomeSummary;
    private String resuscitationStatus;
}
