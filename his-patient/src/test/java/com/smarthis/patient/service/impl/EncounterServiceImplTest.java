package com.smarthis.patient.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.patient.entity.Registration;
import com.smarthis.patient.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncounterServiceImplTest {
    @Mock EncounterMapper encounterMapper;
    @Mock RegistrationMapper registrationMapper;
    @Mock PatientMapper patientMapper;
    @Mock DoctorMapper doctorMapper;
    @Mock DepartmentMapper departmentMapper;
    @Mock BizNoGenerator bizNoGenerator;
    @InjectMocks EncounterServiceImpl service;

    @Test
    void refusesUnpaidRegistrationBeforeOpeningConsultation() {
        Registration reg = new Registration();
        reg.setId(81L);
        reg.setDeleted(0);
        reg.setRegStatus("ACTIVE");
        reg.setPayStatus("UNPAID");
        when(registrationMapper.selectByIdForUpdate(81L)).thenReturn(reg);
        var error = assertThrows(BusinessException.class, () -> service.open(81L, "主诉"));
        assertEquals(ErrorCode.REGISTRATION_PAYMENT_REQUIRED.getCode(), error.getCode());
        verifyNoInteractions(encounterMapper);
    }
}
