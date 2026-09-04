package com.smarthis.common.feign;

import com.smarthis.common.context.UserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignHeaderRelayInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        if (UserContextHolder.get() != null) {
            var ctx = UserContextHolder.get();
            if (ctx.getUserId() != null) {
                template.header("X-User-Id", ctx.getUserId().toString());
            }
            if (ctx.getUsername() != null) {
                template.header("X-Username", ctx.getUsername());
            }
            if (ctx.getRealName() != null) {
                template.header("X-Real-Name", ctx.getRealName());
            }
            if (ctx.getDeptId() != null) {
                template.header("X-Dept-Id", ctx.getDeptId().toString());
            }
            if (ctx.getRoles() != null) {
                template.header("X-Roles", ctx.getRoles());
            }
            if (ctx.getTraceId() != null) {
                template.header("X-Trace-Id", ctx.getTraceId());
            }
        }
    }
}
