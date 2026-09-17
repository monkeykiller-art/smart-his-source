package com.smarthis.auth.service;

import com.smarthis.auth.dto.request.LoginRequest;
import com.smarthis.auth.dto.response.LoginResponse;

import java.util.Map;

public interface AuthService {

    LoginResponse login(LoginRequest request, String ip, String userAgent);

    LoginResponse refreshToken(String refreshToken);

    void logout(String accessToken, String refreshToken);

    Map<String, Object> me(Long userId);
    Map<String, String> setupMfa(Long userId);
    void enableMfa(Long userId, String code);
    void disableMfa(Long userId, String code);
}
