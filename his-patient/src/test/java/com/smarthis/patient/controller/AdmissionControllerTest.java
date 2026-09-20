package com.smarthis.patient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smarthis.common.model.PageResult;
import com.smarthis.patient.dto.request.AdmissionCreateRequest;
import com.smarthis.patient.dto.request.AdmissionDepositRequest;
import com.smarthis.patient.dto.request.AdmissionDischargeRequest;
import com.smarthis.patient.dto.request.AdmissionQueryRequest;
import com.smarthis.patient.dto.request.AdmissionTransferRequest;
import com.smarthis.patient.dto.response.AdmissionVo;
import com.smarthis.patient.service.AdmissionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdmissionControllerTest {
    private final AdmissionService service = mock(AdmissionService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AdmissionController(service)).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void createReturnsAdmissionVo() throws Exception {
        AdmissionVo vo = admissionVo();
        when(service.create(any())).thenReturn(vo);

        AdmissionCreateRequest request = createRequest();
        mvc.perform(post("/api/patient/admissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.admissionStatus").value("ADMITTED"));

        ArgumentCaptor<AdmissionCreateRequest> captor = ArgumentCaptor.forClass(AdmissionCreateRequest.class);
        verify(service).create(captor.capture());
        assertThat(captor.getValue().getPatientId()).isEqualTo(20L);
        assertThat(captor.getValue().getDeptId()).isEqualTo(1L);
    }

    @Test
    void createRejectsMissingRequiredFields() throws Exception {
        mvc.perform(post("/api/patient/admissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdReturnsAdmission() throws Exception {
        when(service.getById(100L)).thenReturn(admissionVo());

        mvc.perform(get("/api/patient/admissions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100L));

        verify(service).getById(100L);
    }

    @Test
    void admitCallsService() throws Exception {
        mvc.perform(put("/api/patient/admissions/100/admit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(service).admit(100L);
    }

    @Test
    void dischargePassesDischargeRequest() throws Exception {
        AdmissionDischargeRequest request = new AdmissionDischargeRequest();
        request.setDischargeType("NORMAL");
        request.setDischargeSummary("康复出院");
        request.setActualDischargeDate(LocalDate.of(2026, 9, 20));

        mvc.perform(put("/api/patient/admissions/100/discharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<AdmissionDischargeRequest> captor = ArgumentCaptor.forClass(AdmissionDischargeRequest.class);
        verify(service).discharge(eq(100L), captor.capture());
        assertThat(captor.getValue().getDischargeType()).isEqualTo("NORMAL");
    }

    @Test
    void transferPassesTransferRequest() throws Exception {
        AdmissionTransferRequest request = new AdmissionTransferRequest();
        request.setTargetDeptId(2L);
        request.setTargetWardId(3L);
        request.setTargetBedId(4L);
        request.setReason("术后转科");

        AdmissionVo transferred = admissionVo();
        transferred.setDeptId(2L);
        transferred.setWardId(3L);
        transferred.setBedId(4L);
        when(service.transfer(eq(100L), any())).thenReturn(transferred);

        mvc.perform(put("/api/patient/admissions/100/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptId").value(2));

        verify(service).transfer(eq(100L), any());
    }

    @Test
    void cancelCallsService() throws Exception {
        mvc.perform(put("/api/patient/admissions/100/cancel"))
                .andExpect(status().isOk());

        verify(service).cancel(100L);
    }

    @Test
    void addDepositPassesDepositRequest() throws Exception {
        AdmissionDepositRequest request = new AdmissionDepositRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setPayMethod("CASH");

        mvc.perform(post("/api/patient/admissions/100/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<AdmissionDepositRequest> captor = ArgumentCaptor.forClass(AdmissionDepositRequest.class);
        verify(service).addDeposit(eq(100L), captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    void listReturnsPaginatedResult() throws Exception {
        when(service.list(any())).thenReturn(new PageResult<>(List.of(admissionVo()), 1L, 1, 20));

        mvc.perform(get("/api/patient/admissions")
                        .param("page", "1").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));

        ArgumentCaptor<AdmissionQueryRequest> captor = ArgumentCaptor.forClass(AdmissionQueryRequest.class);
        verify(service).list(captor.capture());
        assertThat(captor.getValue().getPage()).isEqualTo(1);
    }

    @Test
    void listByPatientReturnsList() throws Exception {
        when(service.listByPatient(20L)).thenReturn(List.of(admissionVo()));

        mvc.perform(get("/api/patient/admissions/patient/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(100L));

        verify(service).listByPatient(20L);
    }

    private static AdmissionVo admissionVo() {
        AdmissionVo vo = new AdmissionVo();
        vo.setId(100L);
        vo.setAdmissionNo("AD20260920001");
        vo.setPatientId(20L);
        vo.setPatientName("测试患者");
        vo.setAdmissionStatus("ADMITTED");
        vo.setDeptId(1L);
        vo.setDeptName("内科");
        vo.setWardId(1L);
        vo.setWardName("一病区");
        vo.setBedId(1L);
        vo.setBedNo("001");
        vo.setAdmissionDate(LocalDate.of(2026, 9, 18));
        return vo;
    }

    private static AdmissionCreateRequest createRequest() {
        AdmissionCreateRequest request = new AdmissionCreateRequest();
        request.setPatientId(20L);
        request.setDeptId(1L);
        request.setDoctorId(5L);
        request.setAdmissionDate(LocalDate.of(2026, 9, 18));
        request.setAdmissionType("ELECTIVE");
        request.setDepositAmount(new BigDecimal("10000.00"));
        return request;
    }
}
