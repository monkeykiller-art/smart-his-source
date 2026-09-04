package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_diagnosis")
public class Diagnosis extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long doctorId;

    private Long icd10Id;

    private String icdCode;

    private String diagnosisName;

    private String diagnosisType;

    private Integer isPrimary;

    private Integer isConfirmed;

    private Integer diagnosisSeq;

    private LocalDate onsetDate;

    private String diagnosisDesc;

    private String diagnosisStatus;
}
