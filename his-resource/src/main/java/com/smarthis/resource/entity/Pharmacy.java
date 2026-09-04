package com.smarthis.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_pharmacy")
public class Pharmacy extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String pharmacyCode;

    private String pharmacyName;

    private String pharmacyType;

    private Long deptId;

    private String location;

    private String phone;

    private String pharmacyStatus;
}
