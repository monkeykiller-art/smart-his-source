package com.smarthis.patient.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitSeqAllocator {

    private static final String KEY_PREFIX = "his:visitseq:";

    private final RedisTemplate<String, Object> redisTemplate;

    public int allocate(Long scheduleId) {
        String key = KEY_PREFIX + scheduleId;
        Long seq = redisTemplate.opsForValue().increment(key);
        int visitSeq = (seq != null) ? seq.intValue() : 1;
        log.debug("Visit seq allocated: scheduleId={}, seq={}", scheduleId, visitSeq);
        return visitSeq;
    }
}
