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
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private Long userId;
    private String username;
    private String realName;
    private Long deptId;
    private String deptName;
    private List<String> roles;
    private List<String> permissions;
}
