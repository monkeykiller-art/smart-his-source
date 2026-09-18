package com.smarthis.clinical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cli_common_phrase")
public class CommonPhrase extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String phraseName;

    private String phraseContent;

    private String phraseType;

    private Long deptId;

    private Long userId;

    private Integer sortOrder;
}
