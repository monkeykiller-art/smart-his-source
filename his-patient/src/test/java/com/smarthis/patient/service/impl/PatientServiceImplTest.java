package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.patient.dto.request.PatientQueryRequest;
import com.smarthis.patient.entity.Patient;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

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

    @BeforeAll
    static void initializePatientMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Patient.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2099000000000000001", "EMPI0001", "张三", "110101199001011234", "13800138000", "9223372036854775807"})
    void searchesAllIdentifiersWithBoundParameters(String keyword) {
        PatientQueryRequest request = new PatientQueryRequest();
        request.setKeyword("  " + keyword + "  ");
        request.setPage(2);
        request.setSize(10);
        when(patientMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Patient> page = invocation.getArgument(0);
            LambdaQueryWrapper<Patient> query = invocation.getArgument(1);
            String sql = query.getSqlSegment();
            assertThat(sql).contains("deleted =", "AND (", "name LIKE", "name_pinyin LIKE", "phone LIKE", "empi_no LIKE", "id_no =", "ORDER BY created_time DESC");
            assertThat(sql).doesNotContain(keyword);
            assertThat(query.getParamNameValuePairs().values()).contains(keyword, "%" + keyword + "%");
            if (keyword.matches("[0-9]+")) {
                assertThat(sql).contains("OR id =");
                assertThat(query.getParamNameValuePairs().values()).contains(Long.valueOf(keyword));
            }
            assertThat(page.getCurrent()).isEqualTo(2);
            assertThat(page.getSize()).isEqualTo(10);
            Patient patient = new Patient();
            patient.setId(2099000000000000001L);
            patient.setName("张三");
            page.setRecords(List.of(patient));
            page.setTotal(11);
            return page;
        });

        var result = service.query(request);

        assertThat(result.getRecords()).singleElement().extracting("id").isEqualTo(2099000000000000001L);
        assertThat(result.getTotal()).isEqualTo(11);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"9223372036854775808", "999999999999999999999999", "0", "-1", "1e3"})
    void doesNotCastOutOfRangeOrInvalidKeywordsToPatientIds(String keyword) {
        PatientQueryRequest request = new PatientQueryRequest();
        request.setKeyword(keyword);
        when(patientMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<Patient> query = invocation.getArgument(1);
            assertThat(query.getSqlSegment()).doesNotContain("OR id =");
            assertThat(query.getParamNameValuePairs().values()).contains(keyword);
            return new Page<Patient>();
        });
        assertThat(service.query(request).getRecords()).isEmpty();
    }

    @Test
    void escapesWildcardsAndKeepsExplicitFiltersOutsideTheKeywordGroup() {
        PatientQueryRequest request = new PatientQueryRequest();
        request.setKeyword("A%_\\B");
        request.setIdType("PASSPORT");
        request.setIdNo("A%_\\B");
        request.setPhone("138");
        when(patientMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<Patient> query = invocation.getArgument(1);
            assertThat(query.getSqlSegment()).contains(") AND id_type =", "AND id_no =", "AND phone LIKE");
            assertThat(query.getParamNameValuePairs().values()).contains("%A\\%\\_\\\\B%", "A%_\\B", "PASSPORT", "%138%");
            return new Page<Patient>();
        });
        assertThat(service.query(request).getTotal()).isZero();
    }

    @Test
    void blankKeywordStillListsOnlyUndeletedPatients() {
        PatientQueryRequest request = new PatientQueryRequest();
        request.setKeyword("  ");
        when(patientMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<Patient> query = invocation.getArgument(1);
            assertThat(query.getSqlSegment()).contains("deleted =").doesNotContain("LIKE", " OR ");
            return new Page<Patient>();
        });
        assertThat(service.query(request).getRecords()).isEmpty();
    }

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
