package com.smarthis.patient.service.impl;

import com.smarthis.patient.mapper.RegistrationMapper;
import com.smarthis.patient.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegistrationBillingSyncJob {
    private final RegistrationMapper registrationMapper;
    private final RegistrationService registrationService;

    @Scheduled(fixedDelayString = "${his.registration.billing-sync-delay-ms:30000}", initialDelay = 30000)
    public void sync() {
        for (Long id : registrationMapper.billingSyncIds()) {
            try {
                registrationService.syncBilling(id);
            } catch (Exception exception) {
                log.warn("Registration billing sync pending; registrationId={}", id);
                registrationMapper.touchBillingAttempt(id);
            }
        }
    }
}
