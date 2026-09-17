package com.smarthis.common.security;

import com.smarthis.common.context.UserContext;
import com.smarthis.common.context.UserContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationAuditAspectTest {
    @AfterEach void clear() { UserContextHolder.clear(); }

    @Test void persistsSuccessfulPermissionProtectedOperation() throws Throwable {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        @SuppressWarnings("unchecked") ObjectProvider<JdbcTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(jdbc);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("BillController.pay(..)");
        when(joinPoint.proceed()).thenReturn("ok");
        RequiresPermission permission = mock(RequiresPermission.class);
        when(permission.value()).thenReturn("operations:payment:create");
        UserContext context = new UserContext(); context.setUserId(1L); context.setUsername("cashier"); context.setTraceId("trace-1");
        UserContextHolder.set(context);

        assertEquals("ok", new OperationAuditAspect(provider).audit(joinPoint, permission));

        verify(jdbc).update(any(String.class), any(), eq(1L), eq("cashier"), eq("operations:payment:create"),
                eq("permission"), eq("BillController.pay(..)"), eq(200), eq(0L), any());
    }
}
