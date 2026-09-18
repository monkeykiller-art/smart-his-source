package com.smarthis.common.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;

public class ServiceAuthInterceptor implements HandlerInterceptor {

    private static final String SERVICE_SCHEME = "Service ";

    private final String serviceToken;

    public ServiceAuthInterceptor(@Value("${his.service.token:}") String serviceToken) {
        this.serviceToken = serviceToken == null ? "" : serviceToken;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.regionMatches(true, 0, SERVICE_SCHEME, 0, SERVICE_SCHEME.length())) {
            return true;
        }
        String token = authorization.substring(SERVICE_SCHEME.length()).trim();
        if (serviceToken.isEmpty() || !constantTimeEquals(token, serviceToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"invalid service token\",\"data\":null}");
            return false;
        }
        UserContext context = new UserContext();
        context.setUserId(0L);
        context.setUsername("service");
        context.setRealName("service");
        context.setRoles("SERVICE");
        context.setPermissions("operations:bill:create,operations:bill:read");
        UserContextHolder.set(context);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext context = UserContextHolder.get();
        if (context != null && "service".equals(context.getUsername())) {
            UserContextHolder.clear();
        }
    }

    private boolean constantTimeEquals(String actual, String expected) {
        if (actual.length() != expected.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < actual.length(); i++) {
            result |= actual.charAt(i) ^ expected.charAt(i);
        }
        return result == 0;
    }
}
