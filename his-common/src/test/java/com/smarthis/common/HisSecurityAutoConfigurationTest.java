package com.smarthis.common;

import com.smarthis.common.autoconfigure.HisCommonAutoConfiguration;
import com.smarthis.common.context.UserContext;
import com.smarthis.common.context.UserContextHolder;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.security.OperationAuditAspect;
import com.smarthis.common.security.PermissionAspect;
import com.smarthis.common.security.RequiresPermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HisSecurityAutoConfigurationTest {
    private final AtomicInteger calls = new AtomicInteger();
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HisCommonAutoConfiguration.class, AopAutoConfiguration.class))
            .withClassLoader(new FilteredClassLoader("org.springframework.data.redis", "org.springframework.kafka"))
            .withBean(ProtectedOperation.class, () -> new ProtectedOperation(calls));

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Test
    void commonAutoConfigurationSecuresAndAuditsActualProxyCalls() {
        signIn("DOCTOR", "patient:read");
        runner.withBean(JdbcTemplate.class, () -> jdbc).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(PermissionAspect.class)
                    .hasSingleBean(OperationAuditAspect.class);
            ProtectedOperation operation = context.getBean(ProtectedOperation.class);
            assertThat(AopUtils.isAopProxy(operation)).isTrue();
            assertThat(operation.read()).isEqualTo("ok");
            assertThat(calls).hasValue(1);
            verify(jdbc).update(contains("INSERT INTO his_auth.auth_operation_log"), any(), eq(1L), eq("doctor"),
                    eq("patient:read"), eq("permission"), any(), eq(200), eq(0L), any());
        });
    }

    @Test
    void missingPermissionPreventsBusinessMethodInvocation() {
        signIn("DOCTOR", "clinical:read");
        runner.run(context -> {
            assertThatThrownBy(() -> context.getBean(ProtectedOperation.class).read())
                    .isInstanceOfSatisfying(BusinessException.class,
                            error -> assertThat(error.getCode()).isEqualTo(ErrorCode.AUTH_NO_PERMISSION.getCode()));
            assertThat(calls).hasValue(0);
        });
    }

    @Test
    void missingSessionPreventsBusinessMethodInvocation() {
        runner.run(context -> {
            assertThatThrownBy(() -> context.getBean(ProtectedOperation.class).read())
                    .isInstanceOfSatisfying(BusinessException.class,
                            error -> assertThat(error.getCode()).isEqualTo(ErrorCode.UNAUTHORIZED.getCode()));
            assertThat(calls).hasValue(0);
        });
    }

    @Test
    void administratorCanInvokeWithoutExplicitPermissions() {
        signIn("ADMIN", null);
        runner.run(context -> {
            assertThat(context.getBean(ProtectedOperation.class).read()).isEqualTo("ok");
            assertThat(calls).hasValue(1);
        });
    }

    @Test
    void existingAspectsAreNotRegisteredTwice() {
        runner.withBean(PermissionAspect.class, PermissionAspect::new)
                .withBean(OperationAuditAspect.class, () -> new OperationAuditAspect(
                        new org.springframework.beans.factory.support.DefaultListableBeanFactory()
                                .getBeanProvider(JdbcTemplate.class)))
                .run(context -> assertThat(context).hasNotFailed().hasSingleBean(PermissionAspect.class)
                        .hasSingleBean(OperationAuditAspect.class));
    }

    @Test
    void classpathWithoutAspectJDoesNotLoadSecurityAspects() {
        runner.withClassLoader(new FilteredClassLoader("org.aspectj", "org.springframework.data.redis", "org.springframework.kafka"))
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean("permissionAspect")
                        .doesNotHaveBean("operationAuditAspect"));
    }

    @Test
    void classpathWithoutJdbcStillEnforcesPermissions() {
        signIn("DOCTOR", "clinical:read");
        runner.withClassLoader(new FilteredClassLoader("org.springframework.jdbc", "org.springframework.data.redis", "org.springframework.kafka"))
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(PermissionAspect.class)
                            .doesNotHaveBean("operationAuditAspect");
                    assertThatThrownBy(() -> context.getBean(ProtectedOperation.class).read())
                            .isInstanceOf(BusinessException.class);
                    assertThat(calls).hasValue(0);
                });
    }

    private void signIn(String roles, String permissions) {
        UserContext context = new UserContext();
        context.setUserId(1L);
        context.setUsername("doctor");
        context.setRoles(roles);
        context.setPermissions(permissions);
        UserContextHolder.set(context);
    }

    public static class ProtectedOperation {
        private final AtomicInteger calls;

        public ProtectedOperation(AtomicInteger calls) {
            this.calls = calls;
        }

        @RequiresPermission("patient:read")
        public String read() {
            calls.incrementAndGet();
            return "ok";
        }
    }
}
