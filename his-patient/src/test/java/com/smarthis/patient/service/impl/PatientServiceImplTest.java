package com.smarthis.patient.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.patient.dto.request.PatientCreateRequest;
import com.smarthis.patient.mapper.PatientIdentifierMapper;
import com.smarthis.patient.mapper.PatientMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private PatientIdentifierMapper identifierMapper;

    @Mock
    private BizNoGenerator bizNoGenerator;

    @InjectMocks
    private PatientServiceImpl service;

    @Test
    void rejectsDuplicateIdentityBeforeCreatingPatient() {
        PatientCreateRequest request = new PatientCreateRequest();
        request.setName("张三");
        request.setIdType("ID_CARD");
        request.setIdNo("110101199001011234");
        request.setPhone("13800138000");
        when(identifierMapper.selectCount(org.mockito.ArgumentMatchers.any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(ErrorCode.PATIENT_DUPLICATE.getCode(), exception.getCode());
        verifyNoInteractions(patientMapper, bizNoGenerator);
    }
}
