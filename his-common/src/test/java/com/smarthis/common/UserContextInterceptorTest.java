package com.smarthis.common;

import com.smarthis.common.context.UserContextHolder;
import com.smarthis.common.context.UserContextInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextInterceptorTest {

    private final UserContextInterceptor interceptor = new UserContextInterceptor();

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void ignoresNullAndMalformedNumericHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "invalid");
        request.addHeader("X-Dept-Id", "null");

        boolean accepted = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(accepted).isTrue();
        assertThat(UserContextHolder.getUserId()).isNull();
        assertThat(UserContextHolder.get().getDeptId()).isNull();
    }
}
