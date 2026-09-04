package com.smarthis.clinical.controller;

import com.smarthis.clinical.dto.request.DiagnosisCreateRequest;
import com.smarthis.clinical.dto.response.DiagnosisVo;
import com.smarthis.clinical.service.DiagnosisService;
import com.smarthis.common.model.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinical/diagnoses")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping
    public ApiResponse<DiagnosisVo> create(@Valid @RequestBody DiagnosisCreateRequest request) {
        return ApiResponse.ok(diagnosisService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DiagnosisVo> update(@PathVariable Long id, @Valid @RequestBody DiagnosisCreateRequest request) {
        return ApiResponse.ok(diagnosisService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        diagnosisService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/encounter/{encounterId}")
    public ApiResponse<List<DiagnosisVo>> listByEncounter(@PathVariable Long encounterId) {
        return ApiResponse.ok(diagnosisService.listByEncounter(encounterId));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<List<DiagnosisVo>> listByAdmission(@PathVariable Long admissionId) {
        return ApiResponse.ok(diagnosisService.listByAdmission(admissionId));
    }
}
