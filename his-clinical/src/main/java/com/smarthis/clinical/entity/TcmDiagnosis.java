package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_tcm_diagnosis")
public class TcmDiagnosis extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String tcmCode;

    private String tcmName;

    private String namePinyin;

    private String syndromeCode;

    private String syndromeName;

    private String category;

    private Integer sortOrder;

    private Integer dictStatus;
}
