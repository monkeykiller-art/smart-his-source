package com.smarthis.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {
    private Long id;
    private String username;
    private String employeeNo;
    private String userType;
    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private Long deptId;
    private String deptName;
    private String userStatus;
    private Integer prescribeRight;
    private Integer antibioticLevel;
    private List<String> roleCodes;
}
