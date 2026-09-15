package com.smarthis.patient.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.event.EventPublisher;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.patient.client.OperationsClient;
import com.smarthis.patient.entity.Patient;
import com.smarthis.patient.entity.Registration;
import com.smarthis.patient.entity.Schedule;
import com.smarthis.patient.dto.request.RegistrationCreateRequest;
import com.smarthis.patient.mapper.DepartmentMapper;
import com.smarthis.patient.mapper.DoctorMapper;
import com.smarthis.patient.mapper.EncounterMapper;
import com.smarthis.patient.mapper.PatientIdentifierMapper;
import com.smarthis.patient.mapper.PatientMapper;
import com.smarthis.patient.mapper.RegistrationMapper;
import com.smarthis.patient.mapper.ScheduleMapper;
import com.smarthis.patient.mapper.TriageMapper;
import com.smarthis.patient.support.EmpiMatcher;
import com.smarthis.patient.support.QuotaManager;
import com.smarthis.patient.support.VisitSeqAllocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock private RegistrationMapper registrationMapper;
    @Mock private PatientMapper patientMapper;
    @Mock private PatientIdentifierMapper identifierMapper;
    @Mock private ScheduleMapper scheduleMapper;
    @Mock private EncounterMapper encounterMapper;
    @Mock private TriageMapper triageMapper;
    @Mock private DoctorMapper doctorMapper;
    @Mock private DepartmentMapper departmentMapper;
    @Mock private EmpiMatcher empiMatcher;
    @Mock private QuotaManager quotaManager;
    @Mock private VisitSeqAllocator visitSeqAllocator;
    @Mock private BizNoGenerator bizNoGenerator;
    @Mock private OperationsClient operationsClient;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks
    private RegistrationServiceImpl service;

    @Test
    void rejectsDuplicateActiveRegistrationBeforeAcquiringQuota() {
        Patient patient = new Patient();
        patient.setId(10L);
        patient.setDeleted(0);
        Schedule schedule = new Schedule();
        schedule.setId(20L);
        schedule.setDeleted(0);
        schedule.setScheduleStatus("ACTIVE");
        schedule.setDeptId(30L);
        schedule.setDoctorId(40L);
        when(patientMapper.selectById(10L)).thenReturn(patient);
        when(scheduleMapper.selectById(20L)).thenReturn(schedule);
        when(registrationMapper.selectCount(any())).thenReturn(1L);

        RegistrationCreateRequest request = new RegistrationCreateRequest();
        request.setPatientId(10L);
        request.setScheduleId(20L);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(ErrorCode.REGISTRATION_DUPLICATE.getCode(), exception.getCode());
        org.mockito.Mockito.verifyNoInteractions(quotaManager);
    }

    @Test
    void requiresRefundForPaidRegistration() {
        Registration registration = new Registration();
        registration.setId(11L);
        registration.setDeleted(0);
        registration.setPayStatus("PAID");
        registration.setRegStatus("ACTIVE");
        when(registrationMapper.selectById(11L)).thenReturn(registration);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.cancel(11L, "患者取消"));

        assertEquals(ErrorCode.REGISTRATION_PAYMENT_REQUIRED.getCode(), exception.getCode());
    }

    @Test
    void refusesRefundForUnpaidRegistration() {
        Registration registration = new Registration();
        registration.setId(12L);
        registration.setDeleted(0);
        registration.setPayStatus("UNPAID");
        registration.setRegStatus("ACTIVE");
        when(registrationMapper.selectById(12L)).thenReturn(registration);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.refund(12L, "患者退号"));

        assertEquals(ErrorCode.REGISTRATION_PAYMENT_REQUIRED.getCode(), exception.getCode());
    }
}
