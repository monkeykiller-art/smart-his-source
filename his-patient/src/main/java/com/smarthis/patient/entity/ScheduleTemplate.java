package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_schedule_template")
public class ScheduleTemplate extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String templateName;

    private Long deptId;

    private Long doctorId;

    private Integer dayOfWeek;

    private String timePeriod;

    private String startTime;

    private String endTime;

    private Integer totalQuota;

    private BigDecimal regFee;

    private String regLevel;

    private String templateStatus;
}
