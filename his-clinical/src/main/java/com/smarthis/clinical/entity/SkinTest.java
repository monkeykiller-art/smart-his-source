package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_skin_test")
public class SkinTest extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long patientId;

    private Long admissionId;

    private Long orderItemId;

    private String drugCode;

    private String drugName;

    private String batchNo;

    private String testResult;

    private LocalDateTime testTime;

    private Long testNurseId;

    private LocalDateTime judgeTime;

    private Long judgeNurseId;

    private String remark;
}
