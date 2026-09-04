package com.smarthis.patient.support;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.patient.entity.Schedule;
import com.smarthis.patient.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuotaManager {

    private static final String QUOTA_KEY_PREFIX = "his:quota:";
    private static final String DECR_IF_GT_ZERO_LUA =
            "local current = tonumber(redis.call('GET', KEYS[1])) " +
            "if current == nil then return -1 end " +
            "if current > 0 then redis.call('DECR', KEYS[1]) return current - 1 " +
            "else return 0 end";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ScheduleMapper scheduleMapper;

    @Transactional
    public int acquire(Long scheduleId) {
        String key = QUOTA_KEY_PREFIX + scheduleId;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(DECR_IF_GT_ZERO_LUA, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(key));

        if (result == null || result == -1) {
            // Cache miss — warm from DB and retry
            warmCache(scheduleId);
            result = redisTemplate.execute(script, Collections.singletonList(key));
            if (result == null || result == -1) {
                throw new BusinessException(ErrorCode.SCHEDULE_NO_QUOTA);
            }
        }

        if (result == 0) {
            // Redis says no quota left; verify against DB (source of truth)
            Schedule schedule = scheduleMapper.selectById(scheduleId);
            if (schedule == null) {
                throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
            }
            int available = schedule.getTotalQuota() - schedule.getUsedQuota();
            if (available <= 0) {
                throw new BusinessException(ErrorCode.SCHEDULE_NO_QUOTA);
            }
            // DB has quota but Redis is stale — re-warm and retry
            warmCache(scheduleId);
            return acquire(scheduleId);
        }

        // Update DB (source of truth) with optimistic lock
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            // Rollback Redis
            redisTemplate.opsForValue().increment(key);
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        schedule.setUsedQuota(schedule.getUsedQuota() + 1);
        int rows = scheduleMapper.updateById(schedule);
        if (rows == 0) {
            // Optimistic lock conflict — rollback Redis and retry
            redisTemplate.opsForValue().increment(key);
            throw new BusinessException(ErrorCode.SCHEDULE_NO_QUOTA);
        }

        log.debug("Quota acquired: scheduleId={}, remaining={}", scheduleId, result - 1);
        return schedule.getTotalQuota() - schedule.getUsedQuota();
    }

    public void release(Long scheduleId) {
        String key = QUOTA_KEY_PREFIX + scheduleId;
        redisTemplate.opsForValue().increment(key);

        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule != null && schedule.getUsedQuota() > 0) {
            schedule.setUsedQuota(schedule.getUsedQuota() - 1);
            scheduleMapper.updateById(schedule);
        }
        log.debug("Quota released: scheduleId={}", scheduleId);
    }

    private void warmCache(Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        String key = QUOTA_KEY_PREFIX + scheduleId;
        int available = schedule.getTotalQuota() - schedule.getUsedQuota();
        redisTemplate.opsForValue().set(key, Math.max(available, 0));
        log.debug("Quota cache warmed: scheduleId={}, available={}", scheduleId, available);
    }
}
