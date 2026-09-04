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
@TableName("cli_exam_request")
public class ExamRequest extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String requestNo;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long deptId;

    private Long doctorId;

    private String requestType;

    private Integer isUrgent;

    private String clinicalDiagnosis;

    private String clinicalInfo;

    private Long requestDeptId;

    private Long executeDeptId;

    private String requestStatus;

    private LocalDateTime requestTime;

    private LocalDateTime resultTime;

    private String resultSummary;

    private String reportNo;

    private Integer isPrinted;

    private LocalDateTime printTime;

    private String remark;
}
