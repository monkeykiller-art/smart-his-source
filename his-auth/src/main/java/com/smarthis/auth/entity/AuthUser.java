package com.smarthis.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarthis.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("auth_user")
public class AuthUser extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;
    private String employeeNo;
    private String passwordHash;
    private String userType;
    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private Long deptId;
    private String deptName;
    private String userStatus;
    private Integer failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime pwdUpdatedAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Integer prescribeRight;
    private Integer antibioticLevel;
    private String remark;
    private Integer mfaEnabled;
    private String mfaSecret;
}
