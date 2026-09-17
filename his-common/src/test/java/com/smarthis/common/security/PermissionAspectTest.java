package com.smarthis.common.security;

import com.smarthis.common.context.UserContext;
import com.smarthis.common.context.UserContextHolder;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PermissionAspectTest {
    private final PermissionAspect aspect = new PermissionAspect();

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void permitsExactPermission() throws Throwable {
        UserContext context = context("DOCTOR", "patient:read,clinical:record:write");
        UserContextHolder.set(context);
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        RequiresPermission required = annotation("clinical:record:write");

        aspect.checkPermission(point, required);

        verify(point).proceed();
    }

    @Test
    void rejectsMissingPermission() {
        UserContextHolder.set(context("DOCTOR", "patient:read"));
        BusinessException error = assertThrows(BusinessException.class,
                () -> aspect.checkPermission(mock(ProceedingJoinPoint.class), annotation("auth:user:list")));
        assertEquals(ErrorCode.AUTH_NO_PERMISSION.getCode(), error.getCode());
    }

    @Test
    void administratorBypassesPermissionList() throws Throwable {
        UserContextHolder.set(context("ADMIN", null));
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        aspect.checkPermission(point, annotation("any:permission"));
        verify(point).proceed();
    }

    private static UserContext context(String roles, String permissions) {
        UserContext context = new UserContext();
        context.setUserId(1L);
        context.setRoles(roles);
        context.setPermissions(permissions);
        return context;
    }

    private static RequiresPermission annotation(String value) {
        return new RequiresPermission() {
            @Override public String value() { return value; }
            @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return RequiresPermission.class; }
        };
    }
}
