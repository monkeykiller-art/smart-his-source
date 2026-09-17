package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.security.RequiresPermission;
import com.smarthis.patient.dto.request.SurgeryApplyRequest;
import com.smarthis.patient.dto.request.SurgeryCompleteRequest;
import com.smarthis.patient.dto.request.SurgeryScheduleRequest;
import com.smarthis.patient.entity.SurgeryCase;
import com.smarthis.patient.service.SurgeryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/surgeries")
@RequiredArgsConstructor
public class SurgeryController {
    private final SurgeryService surgeryService;

    @PostMapping
    @RequiresPermission("clinical:order:create")
    public ApiResponse<SurgeryCase> apply(@Valid @RequestBody SurgeryApplyRequest request) {
        return ApiResponse.ok(surgeryService.apply(request));
    }

    @PutMapping("/{id}/schedule")
    @RequiresPermission("clinical:order:create")
    public ApiResponse<SurgeryCase> schedule(@PathVariable Long id, @Valid @RequestBody SurgeryScheduleRequest request) {
        return ApiResponse.ok(surgeryService.schedule(id, request));
    }

    @PutMapping("/{id}/start")
    @RequiresPermission("clinical:order:create")
    public ApiResponse<SurgeryCase> start(@PathVariable Long id) {
        return ApiResponse.ok(surgeryService.start(id));
    }

    @PutMapping("/{id}/complete")
    @RequiresPermission("clinical:order:create")
    public ApiResponse<SurgeryCase> complete(@PathVariable Long id, @Valid @RequestBody SurgeryCompleteRequest request) {
        return ApiResponse.ok(surgeryService.complete(id, request));
    }

    @GetMapping
    @RequiresPermission("resource:ward:list")
    public ApiResponse<List<SurgeryCase>> list(@RequestParam Long admissionId) {
        return ApiResponse.ok(surgeryService.list(admissionId));
    }
}
