package com.smarthis.pharma.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pha_adr_report")
public class AdrReport extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String reportNo;

    private Long patientId;

    private Long admissionId;

    private String drugCode;

    private String drugName;

    private String batchNo;

    private LocalDateTime adrOnsetTime;

    private String adrType;

    private String adrLevel;

    private String adrDesc;

    private String adrOutcome;

    private String reporterId;

    private String reporterName;

    private Long reportDeptId;

    private LocalDateTime reportTime;

    private String reportStatus;

    private String reviewComment;

    private String reviewerId;

    private LocalDateTime reviewTime;

    private Integer isReportedToAuthority;

    private String remark;
}
