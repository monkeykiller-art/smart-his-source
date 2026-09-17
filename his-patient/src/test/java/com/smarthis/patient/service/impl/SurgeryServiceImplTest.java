package com.smarthis.patient.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.patient.dto.request.SurgeryApplyRequest;
import com.smarthis.patient.dto.request.SurgeryScheduleRequest;
import com.smarthis.patient.entity.Admission;
import com.smarthis.patient.entity.SurgeryCase;
import com.smarthis.patient.mapper.AdmissionMapper;
import com.smarthis.patient.mapper.SurgeryCaseMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SurgeryServiceImplTest {
    private final SurgeryCaseMapper surgeryMapper = mock(SurgeryCaseMapper.class);
    private final AdmissionMapper admissionMapper = mock(AdmissionMapper.class);
    private final BizNoGenerator generator = mock(BizNoGenerator.class);
    private final SurgeryServiceImpl service = new SurgeryServiceImpl(surgeryMapper, admissionMapper, generator);

    @Test
    void followsApplyScheduleStartLifecycle() {
        Admission admission = new Admission();
        admission.setId(1L);
        admission.setPatientId(2L);
        admission.setAdmissionStatus("ADMITTED");
        admission.setDeleted(0);
        when(admissionMapper.selectById(1L)).thenReturn(admission);
        when(generator.next(BizNoType.SURGERY)).thenReturn("SS001");
        SurgeryApplyRequest apply = new SurgeryApplyRequest();
        apply.setAdmissionId(1L);
        apply.setSurgeryName("阑尾切除术");
        apply.setPlannedStartTime(LocalDateTime.now().plusDays(1));
        apply.setSurgeonId(3L);
        SurgeryCase surgery = service.apply(apply);
        surgery.setId(4L);
        when(surgeryMapper.selectById(4L)).thenReturn(surgery);

        SurgeryScheduleRequest schedule = new SurgeryScheduleRequest();
        schedule.setPlannedStartTime(LocalDateTime.now().plusDays(2));
        schedule.setOperatingRoom("OR-1");
        schedule.setAnesthetistId(5L);
        schedule.setAnesthesiaMethod("全麻");
        assertEquals("SCHEDULED", service.schedule(4L, schedule).getSurgeryStatus());
        assertEquals("IN_PROGRESS", service.start(4L).getSurgeryStatus());
        verify(surgeryMapper, org.mockito.Mockito.atLeast(2)).updateById(surgery);
    }

    @Test
    void rejectsSurgeryForDischargedAdmission() {
        Admission admission = new Admission();
        admission.setAdmissionStatus("DISCHARGED");
        admission.setDeleted(0);
        when(admissionMapper.selectById(1L)).thenReturn(admission);
        SurgeryApplyRequest request = new SurgeryApplyRequest();
        request.setAdmissionId(1L);
        assertThrows(BusinessException.class, () -> service.apply(request));
    }
}
