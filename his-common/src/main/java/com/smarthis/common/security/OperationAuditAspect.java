package com.smarthis.common.security;

import com.smarthis.common.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@RequiredArgsConstructor
@ConditionalOnClass({ProceedingJoinPoint.class, JdbcTemplate.class})
public class OperationAuditAspect {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    @Around("@annotation(permission)")
    public Object audit(ProceedingJoinPoint joinPoint, RequiresPermission permission) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            persist(permission.value(), joinPoint.getSignature().toShortString(), "SUCCESS", null, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable error) {
            persist(permission.value(), joinPoint.getSignature().toShortString(), "FAILED", error.getClass().getSimpleName(), System.currentTimeMillis() - start);
            throw error;
        }
    }

    private void persist(String action, String resource, String result, String detail, long executionTime) {
        var context = UserContextHolder.get();
        log.info("AUDIT action={} result={} userId={} traceId={} resource={} time={}ms", action, result,
                context == null ? null : context.getUserId(), context == null ? null : context.getTraceId(), resource, executionTime);
        JdbcTemplate jdbc = jdbcTemplateProvider.getIfAvailable();
        if (jdbc == null) return;
        try {
            jdbc.update("INSERT INTO his_auth.auth_operation_log "
                            + "(id,user_id,username,operation_type,module,description,response_code,execution_time,operation_time) "
                            + "VALUES (?,?,?,?,?,?,?,?,?)",
                    Math.abs(UUID.randomUUID().getMostSignificantBits()),
                    context == null ? null : context.getUserId(), context == null ? null : context.getUsername(),
                    action, "permission", resource + (detail == null ? "" : " (" + detail + ")"),
                    "SUCCESS".equals(result) ? 200 : 500, executionTime, LocalDateTime.now());
        } catch (RuntimeException persistenceError) {
            log.warn("Could not persist operation audit: action={}, reason={}", action, persistenceError.getMessage());
        }
    }
}
