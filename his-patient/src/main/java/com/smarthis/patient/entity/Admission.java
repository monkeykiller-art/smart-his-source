package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_admission")
public class Admission extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String admissionNo;

    private Long patientId;

    private Long encounterId;

    private String admissionType;

    private String admissionStatus;

    private Long deptId;

    private Long doctorId;

    private Long wardId;

    private Long bedId;

    private String insuranceType;

    private String insuranceNo;

    private String insuranceOrg;

    private String emergencyContactName;

    private String emergencyContactPhone;

    private String emergencyContactAddr;

    private LocalDate admissionDate;

    private LocalDate expectedDischargeDate;

    private LocalDate actualDischargeDate;

    private String chiefComplaint;

    private String preliminaryDiagnosis;

    private BigDecimal depositAmount;

    private BigDecimal totalDeposit;

    private String dischargeType;

    private String dischargeSummary;
}
