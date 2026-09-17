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
@TableName("emg_triage")
public class EmergencyTriage extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private String triageNo;
    private Long patientId;
    private Integer triageLevel;
    private LocalDateTime triageTime;
    private String chiefComplaint;
    private String vitalSigns;
    private Long triageNurseId;
    private String triageNurseName;
    private Long targetDeptId;
    private String targetDeptName;
    private String triageStatus;
}
