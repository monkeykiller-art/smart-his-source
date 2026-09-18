package com.smarthis.common;

import com.smarthis.common.context.UserContext;
import com.smarthis.common.context.UserContextHolder;
import com.smarthis.common.feign.FeignHeaderRelayInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeignHeaderRelayInterceptorTest {
    private final FeignHeaderRelayInterceptor interceptor = new FeignHeaderRelayInterceptor();

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void relaysExactPermissionsAlongsideUserIdentity() {
        UserContext context = new UserContext();
        context.setUserId(1L);
        context.setRoles("DOCTOR");
        context.setPermissions("patient:read,operations:bill:create");
        UserContextHolder.set(context);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers().get("X-User-Id")).containsExactly("1");
        assertThat(template.headers().get("X-Roles")).containsExactly("DOCTOR");
        assertThat(template.headers().get("X-Permissions")).containsExactly("patient:read,operations:bill:create");
    }

    @Test
    void absentPermissionsDoNotGrantAccess() {
        UserContext context = new UserContext();
        context.setUserId(1L);
        context.setRoles("DOCTOR");
        UserContextHolder.set(context);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey("X-Permissions");
    }

    @Test
    void absentUserContextDoesNotInventAnIdentity() {
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers()).isEmpty();
    }
}
