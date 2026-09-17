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
@TableName("emg_observation")
public class ObservationRecord extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long triageId;
    private Long patientId;
    private String bedNo;
    private LocalDateTime admitTime;
    private LocalDateTime dischargeTime;
    private String diagnosis;
    private String treatmentPlan;
    private String observationStatus;
    private String dischargeSummary;
}
