package com.smarthis.clinical.controller;

import com.smarthis.clinical.dto.request.MedicalRecordCreateRequest;
import com.smarthis.clinical.dto.request.MedicalRecordUpdateRequest;
import com.smarthis.clinical.dto.response.MedicalRecordVo;
import com.smarthis.clinical.service.MedicalRecordService;
import com.smarthis.common.model.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinical/records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    public ApiResponse<MedicalRecordVo> create(@Valid @RequestBody MedicalRecordCreateRequest request) {
        return ApiResponse.ok(medicalRecordService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MedicalRecordVo> update(@PathVariable Long id, @Valid @RequestBody MedicalRecordUpdateRequest request) {
        return ApiResponse.ok(medicalRecordService.update(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<MedicalRecordVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(medicalRecordService.getById(id));
    }

    @PutMapping("/{id}/sign")
    public ApiResponse<Void> sign(@PathVariable Long id) {
        medicalRecordService.sign(id);
        return ApiResponse.ok();
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<List<MedicalRecordVo>> listByPatient(@PathVariable Long patientId) {
        return ApiResponse.ok(medicalRecordService.listByPatient(patientId));
    }

    @GetMapping("/encounter/{encounterId}")
    public ApiResponse<List<MedicalRecordVo>> listByEncounter(@PathVariable Long encounterId) {
        return ApiResponse.ok(medicalRecordService.listByEncounter(encounterId));
    }
}
