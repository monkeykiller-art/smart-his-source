package com.smarthis.common.autoconfigure;

import com.smarthis.common.feign.FeignErrorDecoder;
import com.smarthis.common.feign.FeignHeaderRelayInterceptor;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
public class HisFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FeignHeaderRelayInterceptor.class)
    public FeignHeaderRelayInterceptor feignHeaderRelayInterceptor() {
        return new FeignHeaderRelayInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(ErrorDecoder.class)
    public ErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    @ConditionalOnMissingBean(Retryer.class)
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
