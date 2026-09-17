package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.security.RequiresPermission;
import com.smarthis.patient.dto.request.AdmissionCreateRequest;
import com.smarthis.patient.dto.request.AdmissionDepositRequest;
import com.smarthis.patient.dto.request.AdmissionDischargeRequest;
import com.smarthis.patient.dto.request.AdmissionQueryRequest;
import com.smarthis.patient.dto.request.AdmissionTransferRequest;
import com.smarthis.patient.dto.response.AdmissionVo;
import com.smarthis.patient.service.AdmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/admissions")
@RequiredArgsConstructor
public class AdmissionController {

    private final AdmissionService admissionService;

    @PostMapping
    @RequiresPermission("resource:bed:manage")
    public ApiResponse<AdmissionVo> create(@Valid @RequestBody AdmissionCreateRequest request) {
        return ApiResponse.ok(admissionService.create(request));
    }

    @GetMapping("/{id}")
    @RequiresPermission("resource:ward:list")
    public ApiResponse<AdmissionVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(admissionService.getById(id));
    }

    @PutMapping("/{id}/admit")
    @RequiresPermission("resource:bed:manage")
    public ApiResponse<Void> admit(@PathVariable Long id) {
        admissionService.admit(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/discharge")
    @RequiresPermission("resource:bed:manage")
    public ApiResponse<Void> discharge(@PathVariable Long id, @Valid @RequestBody AdmissionDischargeRequest request) {
        admissionService.discharge(id, request);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/transfer")
    @RequiresPermission("resource:bed:manage")
    public ApiResponse<AdmissionVo> transfer(@PathVariable Long id,
                                              @Valid @RequestBody AdmissionTransferRequest request) {
        return ApiResponse.ok(admissionService.transfer(id, request));
    }

    @PutMapping("/{id}/cancel")
    @RequiresPermission("resource:bed:manage")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        admissionService.cancel(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/deposit")
    @RequiresPermission("resource:bed:manage")
    public ApiResponse<Void> addDeposit(@PathVariable Long id, @Valid @RequestBody AdmissionDepositRequest request) {
        admissionService.addDeposit(id, request);
        return ApiResponse.ok();
    }

    @GetMapping
    @RequiresPermission("resource:ward:list")
    public ApiResponse<PageResult<AdmissionVo>> list(AdmissionQueryRequest request) {
        return ApiResponse.ok(admissionService.list(request));
    }

    @GetMapping("/patient/{patientId}")
    @RequiresPermission("resource:ward:list")
    public ApiResponse<List<AdmissionVo>> listByPatient(@PathVariable Long patientId) {
        return ApiResponse.ok(admissionService.listByPatient(patientId));
    }
}
