package com.smarthis.clinical.service.impl;

import com.smarthis.clinical.dto.request.ExamRequestResultRequest;
import com.smarthis.clinical.entity.ExamRequest;
import com.smarthis.clinical.entity.MedicalRecord;
import com.smarthis.clinical.mapper.ExamRequestItemMapper;
import com.smarthis.clinical.mapper.ExamRequestMapper;
import com.smarthis.clinical.mapper.MedicalRecordMapper;
import com.smarthis.common.support.BizNoGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamRequestServiceImplTest {
    @Mock private ExamRequestMapper examRequestMapper;
    @Mock private ExamRequestItemMapper examRequestItemMapper;
    @Mock private BizNoGenerator bizNoGenerator;
    @Mock private MedicalRecordMapper medicalRecordMapper;
    private ExamRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExamRequestServiceImpl(examRequestMapper, examRequestItemMapper, bizNoGenerator, medicalRecordMapper);
    }

    @Test
    void reportWritesResultBackToUnsignedMedicalRecord() {
        ExamRequest exam = new ExamRequest();
        exam.setId(12L); exam.setEncounterId(20L); exam.setRequestNo("EX001"); exam.setRequestStatus("IN_PROGRESS"); exam.setDeleted(0);
        when(examRequestMapper.selectById(12L)).thenReturn(exam);
        when(examRequestItemMapper.selectList(any())).thenReturn(List.of());
        MedicalRecord record = new MedicalRecord();
        record.setId(30L); record.setRecordStatus("DRAFT"); record.setAuxiliaryExam("既往结果");
        when(medicalRecordMapper.selectOne(any())).thenReturn(record);
        ExamRequestResultRequest request = new ExamRequestResultRequest();
        request.setResultSummary("胸片未见明显异常"); request.setReportNo("RP001");

        service.report(12L, request);

        ArgumentCaptor<MedicalRecord> captor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(medicalRecordMapper).updateById(captor.capture());
        assertEquals("既往结果\n检验检查[EX001]: 胸片未见明显异常", captor.getValue().getAuxiliaryExam());
    }
}
