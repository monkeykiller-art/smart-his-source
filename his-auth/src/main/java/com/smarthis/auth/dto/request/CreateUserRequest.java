package com.smarthis.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String realName;
    private String employeeNo;
    private String userType;
    private Integer gender;
    private String phone;
    private String email;
    private Long deptId;
    private String deptName;
    private Integer prescribeRight;
    private Integer antibioticLevel;
}
