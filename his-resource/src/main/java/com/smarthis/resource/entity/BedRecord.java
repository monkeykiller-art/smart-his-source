package com.smarthis.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_bed_record")
public class BedRecord extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long bedId;

    private Long patientId;

    private Long admissionId;

    private Long wardId;

    private String bedNo;

    private LocalDateTime admitTime;

    private LocalDateTime dischargeTime;

    private Integer expectedStay;

    private String recordStatus;
}
