package com.smarthis.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_ward")
public class Ward extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String wardCode;

    private String wardName;

    private Long deptId;

    private String wardType;

    private String floorLocation;

    private Integer bedCount;

    private String nurseStation;

    private Long headNurseId;

    private String wardStatus;

    private String description;
}
