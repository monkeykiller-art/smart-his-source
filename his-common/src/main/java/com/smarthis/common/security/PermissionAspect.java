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
                .collect(Collectors.toSet());
        if (roles.contains("ADMIN")) {
            return joinPoint.proceed();
        }
        log.debug("Permission check: required={}, user roles={}", required, roles);
        return joinPoint.proceed();
    }
}
