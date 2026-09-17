package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_inpatient_bed")
public class InpatientBed extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long deptId;
    private Long wardId;
    private String wardName;
    private String bedNo;
    private String bedType;
    private String bedStatus;
    private Long currentAdmissionId;
}
