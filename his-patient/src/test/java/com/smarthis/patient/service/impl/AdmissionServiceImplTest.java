package com.smarthis.patient.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.patient.dto.request.AdmissionTransferRequest;
import com.smarthis.patient.dto.request.AdmissionQueryRequest;
import com.smarthis.patient.entity.Admission;
import com.smarthis.patient.entity.AdmissionTransfer;
import com.smarthis.patient.entity.Patient;
import com.smarthis.patient.mapper.AdmissionMapper;
import com.smarthis.patient.mapper.AdmissionTransferMapper;
import com.smarthis.patient.mapper.InpatientBedMapper;
import com.smarthis.patient.mapper.PatientMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class AdmissionServiceImplTest {
    private final AdmissionMapper admissionMapper = mock(AdmissionMapper.class);
    private final PatientMapper patientMapper = mock(PatientMapper.class);
    private final AdmissionTransferMapper transferMapper = mock(AdmissionTransferMapper.class);
    private final InpatientBedMapper bedMapper = mock(InpatientBedMapper.class);
    private final AdmissionServiceImpl service = new AdmissionServiceImpl(
            admissionMapper, patientMapper, transferMapper, bedMapper, mock(BizNoGenerator.class));

    @Test
    void transferRecordsHistoryAndUpdatesCurrentLocation() {
        Admission admission = admission("ADMITTED");
        Patient patient = new Patient();
        patient.setName("测试患者");
        when(admissionMapper.selectById(10L)).thenReturn(admission);
        when(patientMapper.selectById(20L)).thenReturn(patient);
        when(bedMapper.occupy(4L, 10L)).thenReturn(1);
        AdmissionTransferRequest request = transferRequest(2L, 3L, 4L);

        var result = service.transfer(10L, request);

        ArgumentCaptor<AdmissionTransfer> history = ArgumentCaptor.forClass(AdmissionTransfer.class);
        verify(transferMapper).insert(history.capture());
        assertEquals(1L, history.getValue().getFromDeptId());
        assertEquals(4L, history.getValue().getToBedId());
        assertNotNull(history.getValue().getTransferTime());
        assertEquals(2L, result.getDeptId());
        assertEquals(3L, result.getWardId());
        assertEquals(4L, result.getBedId());
        verify(bedMapper).release(1L, 10L);
    }

    @Test
    void transferRejectsOccupiedTargetBedWithoutChangingLocation() {
        Admission admission = admission("ADMITTED");
        when(admissionMapper.selectById(10L)).thenReturn(admission);
        when(bedMapper.occupy(4L, 10L)).thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.transfer(10L, transferRequest(2L, 3L, 4L)));

        assertEquals(ErrorCode.BED_NOT_AVAILABLE.getCode(), error.getCode());
        verify(transferMapper, never()).insert(org.mockito.ArgumentMatchers.any());
        verify(admissionMapper, never()).updateById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void transferRejectsArchivedAdmission() {
        when(admissionMapper.selectById(10L)).thenReturn(admission("DISCHARGED"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.transfer(10L, transferRequest(2L, 3L, 4L)));

        assertEquals(ErrorCode.ADMISSION_STATUS_INVALID.getCode(), error.getCode());
        verify(transferMapper, never()).insert(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void listLoadsPatientNamesInOneBatch() {
        Admission first = admission("ADMITTED");
        Admission second = admission("PLANNED");
        second.setId(11L);
        second.setPatientId(21L);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Admission> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);
        page.setRecords(java.util.List.of(first, second));
        page.setTotal(2);
        when(admissionMapper.selectPage(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(page);
        Patient firstPatient = new Patient();
        firstPatient.setId(20L);
        firstPatient.setName("患者甲");
        Patient secondPatient = new Patient();
        secondPatient.setId(21L);
        secondPatient.setName("患者乙");
        when(patientMapper.selectBatchIds(java.util.List.of(20L, 21L))).thenReturn(java.util.List.of(firstPatient, secondPatient));

        var result = service.list(new AdmissionQueryRequest());

        assertEquals(2, result.getRecords().size());
        assertEquals("患者甲", result.getRecords().get(0).getPatientName());
        assertEquals("患者乙", result.getRecords().get(1).getPatientName());
        verify(patientMapper, times(1)).selectBatchIds(java.util.List.of(20L, 21L));
        verify(patientMapper, never()).selectById(org.mockito.ArgumentMatchers.any());
    }

    private static Admission admission(String status) {
        Admission admission = new Admission();
        admission.setId(10L);
        admission.setPatientId(20L);
        admission.setAdmissionStatus(status);
        admission.setDeptId(1L);
        admission.setWardId(1L);
        admission.setBedId(1L);
        admission.setDeleted(0);
        return admission;
    }

    private static AdmissionTransferRequest transferRequest(Long deptId, Long wardId, Long bedId) {
        AdmissionTransferRequest request = new AdmissionTransferRequest();
        request.setTargetDeptId(deptId);
        request.setTargetWardId(wardId);
        request.setTargetBedId(bedId);
        request.setReason("术后转科");
        return request;
    }
}
