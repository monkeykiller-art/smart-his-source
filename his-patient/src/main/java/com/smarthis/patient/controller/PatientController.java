package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.patient.dto.request.PatientCreateRequest;
import com.smarthis.patient.dto.request.PatientQueryRequest;
import com.smarthis.patient.dto.request.PatientUpdateRequest;
import com.smarthis.patient.dto.response.PatientVo;
import com.smarthis.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ApiResponse<PatientVo> create(@Valid @RequestBody PatientCreateRequest request) {
        return ApiResponse.ok(patientService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<PatientVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(patientService.getById(id));
    }

    @GetMapping("/idno")
    public ApiResponse<PatientVo> getByIdNo(@RequestParam String idType, @RequestParam String idNo) {
        return ApiResponse.ok(patientService.getByIdNo(idType, idNo));
    }

    @PutMapping("/{id}")
    public ApiResponse<PatientVo> update(@PathVariable Long id, @RequestBody PatientUpdateRequest request) {
        return ApiResponse.ok(patientService.update(id, request));
    }

    @GetMapping
    public ApiResponse<PageResult<PatientVo>> query(PatientQueryRequest request) {
        return ApiResponse.ok(patientService.query(request));
    }
}
