package com.smarthis.common.context;

import lombok.Data;

@Data
public class UserContext {
    private Long userId;
    private String username;
    private String realName;
    private Long deptId;
    private String roles;
    private String traceId;
}
