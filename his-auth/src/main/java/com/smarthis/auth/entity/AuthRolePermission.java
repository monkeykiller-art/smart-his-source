package com.smarthis.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("auth_role_permission")
public class AuthRolePermission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roleId;
    private Long permissionId;
    private String createdBy;
    private LocalDateTime createdTime;
}
