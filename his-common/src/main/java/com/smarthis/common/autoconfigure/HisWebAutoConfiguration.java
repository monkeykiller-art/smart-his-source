package com.smarthis.common.autoconfigure;

import com.smarthis.common.context.ServiceAuthInterceptor;
import com.smarthis.common.context.UserContextInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(WebMvcConfigurer.class)
public class HisWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UserContextInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceAuthInterceptor serviceAuthInterceptor(@Value("${his.service.token:}") String serviceToken) {
        return new ServiceAuthInterceptor(serviceToken);
    }

    @Bean
    public WebMvcConfigurer hisWebMvcConfigurer(UserContextInterceptor userContextInterceptor,
                                                ServiceAuthInterceptor serviceAuthInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(serviceAuthInterceptor).addPathPatterns("/api/**");
                registry.addInterceptor(userContextInterceptor).addPathPatterns("/api/**");
            }
        };
    }
}
