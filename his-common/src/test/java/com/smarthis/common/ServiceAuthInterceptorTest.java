package com.smarthis.common;

import com.smarthis.common.context.ServiceAuthInterceptor;
import com.smarthis.common.context.UserContextHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceAuthInterceptorTest {

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void validServiceTokenPopulatesMinimalUserContext() throws Exception {
        ServiceAuthInterceptor interceptor = new ServiceAuthInterceptor("secret-token");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Service secret-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean accepted = interceptor.preHandle(request, response, new Object());

        assertThat(accepted).isTrue();
        assertThat(UserContextHolder.getUserId()).isEqualTo(0L);
        assertThat(UserContextHolder.getUsername()).isEqualTo("service");
        assertThat(UserContextHolder.get().getRoles()).isEqualTo("SERVICE");
        assertThat(UserContextHolder.get().getPermissions()).isEqualTo("operations:bill:create,operations:bill:read");
    }

    @Test
    void invalidServiceTokenReturnsUnauthorized() throws Exception {
        ServiceAuthInterceptor interceptor = new ServiceAuthInterceptor("secret-token");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Service wrong-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean accepted = interceptor.preHandle(request, response, new Object());

        assertThat(accepted).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    void missingServiceTokenAllowsRequestToProceed() throws Exception {
        ServiceAuthInterceptor interceptor = new ServiceAuthInterceptor("secret-token");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean accepted = interceptor.preHandle(request, response, new Object());

        assertThat(accepted).isTrue();
        assertThat(UserContextHolder.get()).isNull();
    }

    @Test
    void emptyServiceTokenConfigurationRejectsAllServiceTokens() throws Exception {
        ServiceAuthInterceptor interceptor = new ServiceAuthInterceptor("");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Service any-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean accepted = interceptor.preHandle(request, response, new Object());

        assertThat(accepted).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void afterCompletionClearsServiceContext() throws Exception {
        ServiceAuthInterceptor interceptor = new ServiceAuthInterceptor("secret-token");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Service secret-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        interceptor.preHandle(request, response, new Object());

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(UserContextHolder.get()).isNull();
    }
}
