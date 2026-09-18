package com.smarthis.common.autoconfigure;

import com.smarthis.common.security.OperationAuditAspect;
import com.smarthis.common.security.PermissionAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@ConditionalOnClass(ProceedingJoinPoint.class)
public class HisSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PermissionAspect permissionAspect() {
        return new PermissionAspect();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(JdbcTemplate.class)
    static class AuditConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public OperationAuditAspect operationAuditAspect(ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
            return new OperationAuditAspect(jdbcTemplateProvider);
        }
    }
}
