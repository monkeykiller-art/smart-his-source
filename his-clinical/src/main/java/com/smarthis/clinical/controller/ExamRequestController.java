package com.smarthis.clinical.controller;

import com.smarthis.clinical.dto.request.ExamRequestCreateRequest;
import com.smarthis.clinical.dto.response.ExamRequestVo;
import com.smarthis.clinical.dto.request.ExamRequestResultRequest;
import com.smarthis.clinical.service.ExamRequestService;
import com.smarthis.common.model.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinical/exam-requests")
@RequiredArgsConstructor
public class ExamRequestController {

    private final ExamRequestService examRequestService;

    @PostMapping
    public ApiResponse<ExamRequestVo> create(@Valid @RequestBody ExamRequestCreateRequest request) {
        return ApiResponse.ok(examRequestService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExamRequestVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(examRequestService.getById(id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        examRequestService.cancel(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status/{status}")
    public ApiResponse<ExamRequestVo> transition(@PathVariable Long id, @PathVariable String status) {
        return ApiResponse.ok(examRequestService.transition(id, status.toUpperCase()));
    }

    @PutMapping("/{id}/result")
    public ApiResponse<ExamRequestVo> report(@PathVariable Long id, @Valid @RequestBody ExamRequestResultRequest request) {
        return ApiResponse.ok(examRequestService.report(id, request));
    }

    @PutMapping("/{id}/critical/acknowledge")
    public ApiResponse<ExamRequestVo> acknowledgeCritical(@PathVariable Long id, @RequestParam Long userId) {
        return ApiResponse.ok(examRequestService.acknowledgeCritical(id, userId));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<List<ExamRequestVo>> listByPatient(@PathVariable Long patientId) {
        return ApiResponse.ok(examRequestService.listByPatient(patientId));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<List<ExamRequestVo>> listByAdmission(@PathVariable Long admissionId) {
        return ApiResponse.ok(examRequestService.listByAdmission(admissionId));
    }
}
