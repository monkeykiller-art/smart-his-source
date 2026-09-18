package com.smarthis.clinical.service.impl;

import com.smarthis.clinical.mapper.OrderMapper;
import com.smarthis.common.context.UserContext;
import com.smarthis.common.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderBillingRetryJob {
    private final OrderMapper orderMapper;
    private final OrderBillingService billingService;

    @Scheduled(fixedDelayString = "${his.billing.retry-delay-ms:30000}", initialDelay = 30000)
    public void retryPending() {
        UserContext context = new UserContext();
        context.setUserId(0L);
        context.setUsername("service");
        context.setRoles("SERVICE");
        context.setPermissions("operations:bill:create");
        UserContextHolder.set(context);
        try {
            for (Long id : orderMapper.pendingBillIds()) {
                try { billingService.sync(id); }
                catch (Exception exception) { log.warn("Order billing retry failed; orderId={}", id); }
            }
        } finally {
            UserContextHolder.clear();
        }
    }
}
