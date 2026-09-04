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
@TableName("pat_encounter")
public class Encounter extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String encounterNo;

    private Long patientId;

    private Long regId;

    private Long deptId;

    private Long doctorId;

    private String encounterType;

    private String encounterStatus;

    private LocalDate visitDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String chiefComplaint;
}
