package com.smarthis.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_supplier")
public class Supplier extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String supplierCode;

    private String supplierName;

    private String contactPerson;

    private String contactPhone;

    private String address;

    private String licenseNo;

    private String supplierType;

    private String supplierStatus;
}
