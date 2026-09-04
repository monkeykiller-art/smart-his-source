package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_schedule")
public class Schedule extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long deptId;

    private Long doctorId;

    private LocalDate scheduleDate;

    private String timePeriod;

    private String startTime;

    private String endTime;

    private Integer totalQuota;

    private Integer usedQuota;

    private BigDecimal regFee;

    private String regLevel;

    private String scheduleStatus;

    @Version
    private Integer revision;
}
