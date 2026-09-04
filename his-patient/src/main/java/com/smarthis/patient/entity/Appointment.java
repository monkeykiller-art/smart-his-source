package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_appointment")
public class Appointment extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String apptNo;

    private Long patientId;

    private Long scheduleId;

    private Long deptId;

    private Long doctorId;

    private LocalDate apptDate;

    private String timePeriod;

    private String apptSource;

    private String apptStatus;

    private LocalDateTime confirmTime;

    private String cancelReason;

    private LocalDateTime cancelTime;

    private Long regId;
}
