package com.smarthis.common.support;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnClass(RedisTemplate.class)
@RequiredArgsConstructor
public class BizNoGenerator {

    private static final String KEY_PREFIX = "his:seq:";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisTemplate<String, Object> redisTemplate;

    public String next(BizNoType type) {
        String dateStr = LocalDate.now().format(DATE_FMT);
        String key = KEY_PREFIX + type.getPrefix() + ":" + dateStr;
        Long seq = redisTemplate.opsForValue().increment(key);
        return type.getPrefix() + dateStr + String.format("%06d", seq != null ? seq : 1);
    }
}
