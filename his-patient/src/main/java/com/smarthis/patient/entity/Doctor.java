package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_doctor")
public class Doctor extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String employeeNo;

    private String doctorName;

    private String namePinyin;

    private Integer gender;

    private Long deptId;

    private String title;

    private String specialty;

    private Integer prescribeRight;

    private Integer antibioticLevel;

    private String phone;

    private String doctorStatus;
}
