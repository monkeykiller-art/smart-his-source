package com.smarthis.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.auth.dto.request.LoginRequest;
import com.smarthis.auth.dto.response.LoginResponse;
import com.smarthis.auth.entity.AuthLoginLog;
import com.smarthis.auth.entity.AuthUser;
import com.smarthis.auth.mapper.LoginLogMapper;
import com.smarthis.auth.mapper.UserMapper;
import com.smarthis.auth.support.JwtProvider;
import com.smarthis.auth.support.PasswordHasher;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private LoginLogMapper loginLogMapper;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userMapper, loginLogMapper, jwtProvider, redisTemplate);
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockMinutes", 15);
    }

    @Test
    void loginReturnsTokensAndStoresRefreshToken() {
        AuthUser user = activeUser();
        LoginRequest request = loginRequest("correct-password");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(userMapper.selectRoleCodesByUserId(100L)).thenReturn(List.of("DOCTOR", "USER"));
        when(userMapper.selectPermCodesByUserId(100L)).thenReturn(List.of("patient:read"));
        when(jwtProvider.createAccessToken(100L, "doctor", "Doctor Zhang", 20L, "DOCTOR,USER", "patient:read"))
                .thenReturn("access-token");
        when(jwtProvider.createRefreshToken(100L)).thenReturn("refresh-token");
        when(jwtProvider.getJti("refresh-token")).thenReturn("refresh-jti");
        when(jwtProvider.getRefreshTokenTtlSeconds()).thenReturn(604800L);
        when(jwtProvider.getAccessTokenTtlSeconds()).thenReturn(1800L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LoginResponse response = authService.login(request, "127.0.0.1", "test-agent");

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getRoles()).containsExactly("DOCTOR", "USER");
        assertThat(response.getPermissions()).containsExactly("patient:read");
        verify(valueOperations).set(
                "his:auth:refresh:refresh-jti", "100", 604800L, TimeUnit.SECONDS);

        ArgumentCaptor<AuthUser> userUpdate = ArgumentCaptor.forClass(AuthUser.class);
        verify(userMapper).updateById(userUpdate.capture());
        assertThat(userUpdate.getValue().getFailedAttempts()).isZero();
        assertThat(userUpdate.getValue().getLastLoginIp()).isEqualTo("127.0.0.1");
        assertThat(userUpdate.getValue().getLastLoginAt()).isNotNull();

        ArgumentCaptor<AuthLoginLog> loginLog = ArgumentCaptor.forClass(AuthLoginLog.class);
        verify(loginLogMapper).insert(loginLog.capture());
        assertThat(loginLog.getValue().getLoginResult()).isEqualTo("SUCCESS");
    }

    @Test
    void fifthWrongPasswordLocksAccountForConfiguredPeriod() {
        AuthUser user = activeUser();
        user.setFailedAttempts(4);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        LocalDateTime before = LocalDateTime.now();
        assertThatThrownBy(() -> authService.login(
                loginRequest("wrong-password"), "127.0.0.1", "test-agent"))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getCode()).isEqualTo(ErrorCode.AUTH_LOGIN_FAILED.getCode()));

        ArgumentCaptor<AuthUser> update = ArgumentCaptor.forClass(AuthUser.class);
        verify(userMapper).updateById(update.capture());
        assertThat(update.getValue().getFailedAttempts()).isZero();
        assertThat(update.getValue().getLockedUntil())
                .isBetween(before.plusMinutes(15), LocalDateTime.now().plusMinutes(15));
        verify(jwtProvider, never()).createAccessToken(any(), any(), any(), any(), any(), any());
    }

    @Test
    void loginRejectsMissingMfaCodeWhenEnabled() {
        AuthUser user = activeUser();
        user.setMfaEnabled(1);
        user.setMfaSecret("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        assertThatThrownBy(() -> authService.login(loginRequest("correct-password"), "127.0.0.1", "test-agent"))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getCode()).isEqualTo(ErrorCode.AUTH_MFA_REQUIRED.getCode()));
        verify(jwtProvider, never()).createAccessToken(any(), any(), any(), any(), any(), any());
    }

    @Test
    void loginRejectsAccountWhoseLockHasNotExpired() {
        AuthUser user = activeUser();
        user.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        assertThatThrownBy(() -> authService.login(
                loginRequest("correct-password"), "127.0.0.1", "test-agent"))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getCode()).isEqualTo(ErrorCode.AUTH_ACCOUNT_LOCKED.getCode()));

        verify(userMapper, never()).updateById(any());
    }

    @Test
    void refreshTokenReturnsNewAccessTokenForActiveSession() {
        Claims claims = mock(Claims.class);
        AuthUser user = activeUser();
        when(jwtProvider.parseToken("refresh-token")).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(claims.getId()).thenReturn("refresh-jti");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("his:auth:refresh:refresh-jti")).thenReturn("100");
        when(userMapper.selectById(100L)).thenReturn(user);
        when(userMapper.selectRoleCodesByUserId(100L)).thenReturn(List.of("DOCTOR"));
        when(userMapper.selectPermCodesByUserId(100L)).thenReturn(List.of("patient:read"));
        when(jwtProvider.createAccessToken(100L, "doctor", "Doctor Zhang", 20L, "DOCTOR", "patient:read"))
                .thenReturn("new-access-token");
        when(jwtProvider.getAccessTokenTtlSeconds()).thenReturn(1800L);

        LoginResponse response = authService.refreshToken("refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNull();
        assertThat(response.getUserId()).isEqualTo(100L);
        assertThat(response.getRoles()).containsExactly("DOCTOR");
        assertThat(response.getPermissions()).containsExactly("patient:read");
    }

    @Test
    void expiredRefreshTokenReturnsTokenExpiredError() {
        when(jwtProvider.parseToken("expired-token")).thenThrow(mock(ExpiredJwtException.class));

        assertThatThrownBy(() -> authService.refreshToken("expired-token"))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getCode()).isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED.getCode()));
    }

    @Test
    void refreshRejectsAccessTokenAndRevokedSession() {
        Claims accessClaims = mock(Claims.class);
        when(jwtProvider.parseToken("access-token")).thenReturn(accessClaims);
        when(accessClaims.get("type", String.class)).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshToken("access-token"))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getCode()).isEqualTo(ErrorCode.AUTH_REFRESH_FAILED.getCode()));

        Claims revokedClaims = mock(Claims.class);
        when(jwtProvider.parseToken("revoked-token")).thenReturn(revokedClaims);
        when(revokedClaims.get("type", String.class)).thenReturn("refresh");
        when(revokedClaims.getId()).thenReturn("revoked-jti");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("his:auth:refresh:revoked-jti")).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshToken("revoked-token"))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getCode()).isEqualTo(ErrorCode.AUTH_REFRESH_FAILED.getCode()));
    }

    private static LoginRequest loginRequest(String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername("doctor");
        request.setPassword(password);
        return request;
    }

    private static AuthUser activeUser() {
        AuthUser user = new AuthUser();
        user.setId(100L);
        user.setUsername("doctor");
        user.setPasswordHash(PasswordHasher.hash("correct-password"));
        user.setRealName("Doctor Zhang");
        user.setDeptId(20L);
        user.setDeptName("Internal Medicine");
        user.setUserStatus("ACTIVE");
        user.setFailedAttempts(0);
        user.setDeleted(0);
        return user;
    }
}
