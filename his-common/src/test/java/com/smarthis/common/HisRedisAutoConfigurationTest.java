package com.smarthis.common;

import com.smarthis.common.autoconfigure.HisRedisAutoConfiguration;
import com.smarthis.common.support.BizNoGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class HisRedisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HisRedisAutoConfiguration.class))
            .withBean(ConcurrentMapCacheManager.class, ConcurrentMapCacheManager::new)
            .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class));

    @Test
    void registersBusinessNumberGeneratorForServiceModules() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(BizNoGenerator.class);
            assertThat(context).hasSingleBean(org.springframework.data.redis.core.RedisTemplate.class);
        });
    }
}
