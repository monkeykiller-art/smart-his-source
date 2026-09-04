package com.smarthis.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.auth.dto.request.LoginRequest;
import com.smarthis.auth.dto.response.LoginResponse;
import com.smarthis.auth.entity.AuthLoginLog;
import com.smarthis.auth.entity.AuthUser;
import com.smarthis.auth.mapper.LoginLogMapper;
import com.smarthis.auth.mapper.UserMapper;
import com.smarthis.auth.service.AuthService;
import com.smarthis.auth.support.JwtProvider;
import com.smarthis.auth.support.PasswordHasher;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Value("${his.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${his.security.lock-minutes:15}")
    private int lockMinutes;

    @Override
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        AuthUser user = userMapper.selectOne(
                new LambdaQueryWrapper<AuthUser>()
                        .eq(AuthUser::getUsername, request.getUsername())
                        .eq(AuthUser::getDeleted, 0));

        if (user == null) {
            logLogin(null, request.getUsername(), "LOGIN", "FAILED", ip, userAgent, "user not found");
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            logLogin(user.getId(), user.getUsername(), "LOGIN", "LOCKED", ip, userAgent, null);
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }

        if (!PasswordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            int attempts = (user.getFailedAttempts() != null ? user.getFailedAttempts() : 0) + 1;
            AuthUser update = new AuthUser();
            update.setId(user.getId());
            update.setFailedAttempts(attempts);
            if (attempts >= maxFailedAttempts) {
                update.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
                update.setFailedAttempts(0);
            }
            update.setUpdatedBy("system");
            update.setUpdatedTime(LocalDateTime.now());
            userMapper.updateById(update);
            logLogin(user.getId(), user.getUsername(), "LOGIN", "FAILED", ip, userAgent, "wrong password");
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getId());
        String rolesStr = String.join(",", roleCodes);

        String accessToken = jwtProvider.createAccessToken(
                user.getId(), user.getUsername(), user.getRealName(), user.getDeptId(), rolesStr);
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        String jti = jwtProvider.getJti(refreshToken);
        redisTemplate.opsForValue().set(
                "his:auth:refresh:" + jti,
                String.valueOf(user.getId()),
                jwtProvider.getRefreshTokenTtlSeconds(),
                TimeUnit.SECONDS);

        AuthUser update = new AuthUser();
        update.setId(user.getId());
        update.setFailedAttempts(0);
        update.setLockedUntil(null);
        update.setLastLoginAt(LocalDateTime.now());
        update.setLastLoginIp(ip);
        update.setUpdatedBy("system");
        update.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(update);

        logLogin(user.getId(), user.getUsername(), "LOGIN", "SUCCESS", ip, userAgent, null);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProvider.getAccessTokenTtlSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .deptId(user.getDeptId())
                .deptName(user.getDeptName())
                .roles(roleCodes)
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        Claims claims;
        try {
            claims = jwtProvider.parseToken(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_FAILED);
        }

        String jti = claims.getId();
        String redisKey = "his:auth:refresh:" + jti;
        String storedUserId = redisTemplate.opsForValue().get(redisKey);
        if (storedUserId == null) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_FAILED);
        }

        Long userId = Long.parseLong(storedUserId);
        AuthUser user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1 || "LOCKED".equals(user.getUserStatus())) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }

        List<String> roleCodes = userMapper.selectRoleCodesByUserId(userId);
        String rolesStr = String.join(",", roleCodes);

        String newAccessToken = jwtProvider.createAccessToken(
                userId, user.getUsername(), user.getRealName(), user.getDeptId(), rolesStr);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtProvider.getAccessTokenTtlSeconds())
                .userId(userId)
                .username(user.getUsername())
                .realName(user.getRealName())
                .deptId(user.getDeptId())
                .deptName(user.getDeptName())
                .roles(roleCodes)
                .build();
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null) {
            try {
                String jti = jwtProvider.getJti(accessToken);
                redisTemplate.opsForValue().set(
                        "his:auth:blacklist:" + jti, "1",
                        jwtProvider.getAccessTokenTtlSeconds(), TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // token already invalid, no need to blacklist
            }
        }
        if (refreshToken != null) {
            try {
                String jti = jwtProvider.getJti(refreshToken);
                redisTemplate.delete("his:auth:refresh:" + jti);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Map<String, Object> me(Long userId) {
        AuthUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }
        List<String> roles = userMapper.selectRoleCodesByUserId(userId);
        List<String> perms = userMapper.selectPermCodesByUserId(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("employeeNo", user.getEmployeeNo());
        result.put("deptId", user.getDeptId());
        result.put("deptName", user.getDeptName());
        result.put("userType", user.getUserType());
        result.put("prescribeRight", user.getPrescribeRight());
        result.put("antibioticLevel", user.getAntibioticLevel());
        result.put("roles", roles);
        result.put("permissions", perms);
        return result;
    }

    private void logLogin(Long userId, String username, String loginType,
                          String result, String ip, String userAgent, String remark) {
        AuthLoginLog logEntry = new AuthLoginLog();
        logEntry.setId(Math.abs(UUID.randomUUID().getMostSignificantBits()));
        logEntry.setUserId(userId);
        logEntry.setUsername(username);
        logEntry.setLoginType(loginType);
        logEntry.setLoginResult(result);
        logEntry.setLoginIp(ip);
        logEntry.setUserAgent(userAgent != null && userAgent.length() > 2000
                ? userAgent.substring(0, 2000) : userAgent);
        logEntry.setLoginTime(LocalDateTime.now());
        logEntry.setRemark(remark);
        loginLogMapper.insert(logEntry);
    }
}
