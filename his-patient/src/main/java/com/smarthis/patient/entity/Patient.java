package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_patient")
public class Patient extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String empiNo;

    private String name;

    private String namePinyin;

    private Integer gender;

    private LocalDate birthDate;

    private String ageDisplay;

    private String idType;

    private String idNo;

    private String nationality;

    private String nation;

    private String maritalStatus;

    private String occupation;

    private String phone;

    private String phoneBackup;

    private String address;

    private String bloodType;

    private String allergyHistory;

    private String insuranceType;

    private String insuranceNo;

    private String patientType;

    private String patientStatus;

    private String source;
}
