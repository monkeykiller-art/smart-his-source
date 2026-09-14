package com.smarthis.common.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserContext ctx = new UserContext();
        ctx.setUserId(parseLongOrNull(request.getHeader("X-User-Id")));
        ctx.setUsername(request.getHeader("X-Username"));
        ctx.setRealName(request.getHeader("X-Real-Name"));
        ctx.setDeptId(parseLongOrNull(request.getHeader("X-Dept-Id")));
        ctx.setRoles(request.getHeader("X-Roles"));
        ctx.setTraceId(request.getHeader("X-Trace-Id"));
        UserContextHolder.set(ctx);
        return true;
    }

    /**
     * Upstream relays (gateway, Feign, tests) may forward a literal "null" or any
     * non numeric header value. Degrading to null keeps the request flowing instead
     * of failing the whole request with NumberFormatException.
     */
    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
