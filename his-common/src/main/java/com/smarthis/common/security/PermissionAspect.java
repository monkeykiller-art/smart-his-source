package com.smarthis.common.security;

import com.smarthis.common.context.UserContextHolder;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
public class PermissionAspect {

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        String required = requiresPermission.value();
        var ctx = UserContextHolder.get();
        if (ctx == null || ctx.getRoles() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Set<String> roles = Arrays.stream(ctx.getRoles().split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toSet());
        if (roles.contains("ADMIN")) {
            return joinPoint.proceed();
        }
        Set<String> permissions = ctx.getPermissions() == null ? Set.of() :
                Arrays.stream(ctx.getPermissions().split(","))
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .collect(Collectors.toSet());
        if (!matchesPermission(permissions, required)) {
            log.warn("Permission denied: required={}, userId={}, roles={}", required, ctx.getUserId(), roles);
            throw new BusinessException(ErrorCode.AUTH_NO_PERMISSION);
        }
        return joinPoint.proceed();
    }

    private boolean matchesPermission(Set<String> granted, String required) {
        if (granted.contains("*") || granted.contains(required)) {
            return true;
        }
        for (String perm : granted) {
            if (perm.endsWith(":*") && required.startsWith(perm.substring(0, perm.length() - 1))) {
                return true;
            }
        }
        return false;
    }
}
