package com.smarthis.patient.controller;

import com.smarthis.common.model.PageResult;
import com.smarthis.patient.dto.request.PatientQueryRequest;
import com.smarthis.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientControllerTest {
    private final PatientService service = mock(PatientService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new PatientController(service)).build();

    @ParameterizedTest
    @ValueSource(strings = {"/api/patient/patients/search", "/api/patient/patients"})
    void bindsUnifiedSearchKeywordAndPagination(String path) throws Exception {
        when(service.query(any())).thenReturn(new PageResult<>(List.of(), 0L, 2, 10));
        mvc.perform(get(path).param("keyword", "2099000000000000001").param("page", "2").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.page").value(2));
        ArgumentCaptor<PatientQueryRequest> request = ArgumentCaptor.forClass(PatientQueryRequest.class);
        verify(service).query(request.capture());
        assertThat(request.getValue().getKeyword()).isEqualTo("2099000000000000001");
        assertThat(request.getValue().getSize()).isEqualTo(10);
    }

    @Test
    void supportsAnEmptySearchWithDefaultPagination() throws Exception {
        when(service.query(any())).thenReturn(new PageResult<>(List.of(), 0L, 1, 20));
        mvc.perform(get("/api/patient/patients/search")).andExpect(status().isOk());
        ArgumentCaptor<PatientQueryRequest> request = ArgumentCaptor.forClass(PatientQueryRequest.class);
        verify(service).query(request.capture());
        assertThat(request.getValue().getKeyword()).isNull();
        assertThat(request.getValue().getPage()).isEqualTo(1);
        assertThat(request.getValue().getSize()).isEqualTo(20);
    }
}
