package com.smarthis.auth.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthis.auth.entity.AuthOperationLog;
import com.smarthis.auth.mapper.OperationLogMapper;
import com.smarthis.common.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint point, OperationLog opLog) throws Throwable {
        long start = System.currentTimeMillis();
        Integer responseCode = 200;

        try {
            return point.proceed();
        } catch (Throwable t) {
            responseCode = 500;
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;
            saveLog(opLog, point, responseCode, duration);
        }
    }

    private void saveLog(OperationLog opLog, ProceedingJoinPoint point,
                         Integer responseCode, long duration) {
        try {
            AuthOperationLog logEntry = new AuthOperationLog();
            logEntry.setId(Math.abs(UUID.randomUUID().getMostSignificantBits()));
            logEntry.setUserId(UserContextHolder.getUserId());
            logEntry.setUsername(UserContextHolder.getUsername());
            logEntry.setOperationType(opLog.action());
            logEntry.setModule(opLog.module());
            logEntry.setDescription(opLog.description().isBlank()
                    ? opLog.action() : opLog.description());

            ServletRequestAttributes attrs = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logEntry.setRequestMethod(request.getMethod());
                logEntry.setRequestUrl(request.getRequestURI());
                logEntry.setIp(getClientIp(request));
            }

            try {
                Object[] args = point.getArgs();
                String requestParams = objectMapper.writeValueAsString(args);
                if (requestParams.length() > 4000) {
                    requestParams = requestParams.substring(0, 4000);
                }
                logEntry.setRequestParams(requestParams);
            } catch (Exception e) {
                logEntry.setRequestParams("[serialize failed]");
            }

            logEntry.setResponseCode(responseCode);
            logEntry.setExecutionTime(duration);
            logEntry.setOperationTime(LocalDateTime.now());

            operationLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("failed to save operation log", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
