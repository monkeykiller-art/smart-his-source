package com.smarthis.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.emergency.dto.request.EmergencyTriageCreateRequest;
import com.smarthis.emergency.entity.EmergencyTriage;
import com.smarthis.emergency.mapper.EmergencyTriageMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmergencyTriageServiceImplTest {
    private final EmergencyTriageMapper mapper = mock(EmergencyTriageMapper.class);
    private final BizNoGenerator generator = mock(BizNoGenerator.class);
    private final EmergencyTriageServiceImpl service = new EmergencyTriageServiceImpl(mapper, generator);

    @Test
    void createsWaitingTriageAndOrdersQueueByServiceQuery() {
        when(generator.next(BizNoType.EMERGENCY)).thenReturn("JZ20260917000001");
        EmergencyTriageCreateRequest request = new EmergencyTriageCreateRequest();
        request.setPatientId(10L);
        request.setTriageLevel(2);
        request.setChiefComplaint("胸痛");
        request.setTriageNurseId(20L);

        EmergencyTriage created = service.create(request);

        assertEquals("WAITING", created.getTriageStatus());
        assertEquals("JZ20260917000001", created.getTriageNo());
        verify(mapper).insert(created);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(created));
        assertEquals(List.of(created), service.queue());
    }

    @Test
    void terminalTriageCannotBeReopened() {
        EmergencyTriage triage = new EmergencyTriage();
        triage.setDeleted(0);
        triage.setTriageStatus("COMPLETED");
        when(mapper.selectById(1L)).thenReturn(triage);

        assertThrows(BusinessException.class, () -> service.updateStatus(1L, "WAITING"));
        verify(mapper, never()).updateById(any());
    }
}
