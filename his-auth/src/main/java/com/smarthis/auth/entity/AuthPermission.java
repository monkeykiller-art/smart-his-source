package com.smarthis.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("auth_permission")
public class AuthPermission extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String permCode;
    private String permName;
    private String permType;
    private Long parentId;
    private String moduleCode;
    private String resourcePath;
    private String httpMethod;
    private String icon;
    private Integer sortOrder;
    private String permStatus;
}
