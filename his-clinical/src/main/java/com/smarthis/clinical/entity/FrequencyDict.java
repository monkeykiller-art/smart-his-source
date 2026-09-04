package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_frequency_dict")
public class FrequencyDict extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String freqCode;

    private String freqName;

    private String namePinyin;

    private Integer dailyTimes;

    private String freqDesc;

    private Integer sortOrder;

    private Integer dictStatus;
}
