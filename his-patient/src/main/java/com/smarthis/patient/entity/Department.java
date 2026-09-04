package com.smarthis.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_department")
public class Department extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String deptCode;

    private String deptName;

    private String deptType;

    private Long parentId;

    private Integer sortOrder;

    private String deptStatus;

    private String description;
}
