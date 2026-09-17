package com.smarthis.emergency.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.emergency.dto.request.ObservationCreateRequest;
import com.smarthis.emergency.dto.request.ResuscitationCompleteRequest;
import com.smarthis.emergency.entity.ResuscitationRecord;
import com.smarthis.emergency.mapper.ObservationRecordMapper;
import com.smarthis.emergency.mapper.ResuscitationRecordMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmergencyCareServiceImplTest {
    private final ResuscitationRecordMapper resuscitationMapper = mock(ResuscitationRecordMapper.class);
    private final ObservationRecordMapper observationMapper = mock(ObservationRecordMapper.class);
    private final EmergencyCareServiceImpl service = new EmergencyCareServiceImpl(resuscitationMapper, observationMapper);

    @Test void completesActiveResuscitationWithOutcome() {
        ResuscitationRecord record = new ResuscitationRecord();
        record.setId(1L); record.setDeleted(0); record.setResuscitationStatus("IN_PROGRESS");
        when(resuscitationMapper.selectById(1L)).thenReturn(record);
        ResuscitationCompleteRequest request = new ResuscitationCompleteRequest();
        request.setOutcome("STABLE"); request.setOutcomeSummary("生命体征稳定");
        assertEquals("COMPLETED", service.completeResuscitation(1L, request).getResuscitationStatus());
        verify(resuscitationMapper).updateById(record);
    }

    @Test void rejectsCompletingArchivedResuscitationTwice() {
        ResuscitationRecord record = new ResuscitationRecord();
        record.setDeleted(0); record.setResuscitationStatus("COMPLETED");
        when(resuscitationMapper.selectById(1L)).thenReturn(record);
        assertThrows(BusinessException.class, () -> service.completeResuscitation(1L, new ResuscitationCompleteRequest()));
    }

    @Test void admitsObservationWithBedAndDiagnosis() {
        ObservationCreateRequest request = new ObservationCreateRequest();
        request.setTriageId(2L); request.setPatientId(3L); request.setBedNo("L01"); request.setDiagnosis("胸痛待查");
        var result = service.admitObservation(request);
        assertEquals("ADMITTED", result.getObservationStatus());
        verify(observationMapper).insert(result);
    }
}
