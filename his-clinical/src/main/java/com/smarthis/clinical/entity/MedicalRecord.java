package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_medical_record")
public class MedicalRecord extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String recordNo;

    private Long encounterId;

    private Long admissionId;

    private Long patientId;

    private Long deptId;

    private Long doctorId;

    private String recordType;

    private Long templateId;

    private String title;

    private String chiefComplaint;

    private String presentIllness;

    private String pastHistory;

    private String allergyHistory;

    private String physicalExam;

    private String auxiliaryExam;

    private String diagnosisDesc;

    private String treatmentPlan;

    private String recordContent;

    private String recordStatus;

    private LocalDateTime signTime;

    private String signCertSn;

    private BigDecimal qualityScore;

    private String qualityResult;
}
