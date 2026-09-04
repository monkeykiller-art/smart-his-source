package com.smarthis.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("auth_login_log")
public class AuthLoginLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String username;
    private String loginType;
    private String loginResult;
    private String loginIp;
    private String userAgent;
    private LocalDateTime loginTime;
    private String remark;
}
