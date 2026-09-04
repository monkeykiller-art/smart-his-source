package com.smarthis.common.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserContext ctx = new UserContext();
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            ctx.setUserId(Long.parseLong(userId));
        }
        ctx.setUsername(request.getHeader("X-Username"));
        ctx.setRealName(request.getHeader("X-Real-Name"));
        String deptId = request.getHeader("X-Dept-Id");
        if (deptId != null && !deptId.isBlank()) {
            ctx.setDeptId(Long.parseLong(deptId));
        }
        ctx.setRoles(request.getHeader("X-Roles"));
        ctx.setTraceId(request.getHeader("X-Trace-Id"));
        UserContextHolder.set(ctx);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
