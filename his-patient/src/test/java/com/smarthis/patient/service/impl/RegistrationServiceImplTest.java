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
import com.smarthis.common.model.ApiResponse;
import com.smarthis.patient.entity.Encounter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

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

    @Test
    void linksReturnedBillIdAndSendsRegistrationAmountAndEncounter() {
        Patient patient = new Patient();
        patient.setId(10L);
        patient.setDeleted(0);
        Schedule schedule = new Schedule();
        schedule.setId(20L);
        schedule.setDeleted(0);
        schedule.setScheduleStatus("ACTIVE");
        schedule.setScheduleDate(LocalDate.now().plusDays(1));
        schedule.setRegFee(new BigDecimal("12.50"));
        when(patientMapper.selectById(10L)).thenReturn(patient);
        when(scheduleMapper.selectById(20L)).thenReturn(schedule);
        when(bizNoGenerator.next(any())).thenReturn("MZ123");
        when(registrationMapper.insert(any(Registration.class))).thenAnswer(invocation -> {
            ((Registration) invocation.getArgument(0)).setId(81L);
            return 1;
        });
        when(encounterMapper.insert(any(Encounter.class))).thenAnswer(invocation -> {
            ((Encounter) invocation.getArgument(0)).setId(82L);
            return 1;
        });
        when(operationsClient.createRegistrationBill(any())).thenAnswer(invocation -> {
            Map<String, Object> payload = invocation.getArgument(0);
            assertEquals(81L, payload.get("regId"));
            assertEquals(82L, payload.get("encounterId"));
            assertEquals(new BigDecimal("12.50"), payload.get("amount"));
            assertEquals("OUTPATIENT", payload.get("visitType"));
            return ApiResponse.ok(Map.<String, Object>of("id", 91L));
        });
        RegistrationCreateRequest request = new RegistrationCreateRequest();
        request.setPatientId(10L);
        request.setScheduleId(20L);
        var result = service.create(request);
        assertEquals(91L, result.getBillId());
        assertEquals(schedule.getScheduleDate(), result.getRegDate());
    }
}
