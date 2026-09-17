package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.pharma.dto.request.DispenseCreateRequest;
import com.smarthis.pharma.dto.response.DispenseVo;
import com.smarthis.pharma.service.DispenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharma/dispenses")
@RequiredArgsConstructor
public class DispenseController {
    private final DispenseService dispenseService;

    @PostMapping
    public ApiResponse<DispenseVo> dispense(@Valid @RequestBody DispenseCreateRequest request) {
        return ApiResponse.ok(dispenseService.dispense(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DispenseVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(dispenseService.getById(id));
    }

    @GetMapping("/{id}/print")
    public ApiResponse<DispenseVo> print(@PathVariable Long id) {
        return ApiResponse.ok(dispenseService.getById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<List<DispenseVo>> listByPatient(@PathVariable Long patientId) {
        return ApiResponse.ok(dispenseService.listByPatient(patientId));
    }

    @PutMapping("/{id}/return")
    public ApiResponse<DispenseVo> returnAll(@PathVariable Long id,
                                             @RequestParam(required = false) Long operatorId,
                                             @RequestParam(required = false) String operatorName,
                                             @RequestParam(required = false) String reason) {
        return ApiResponse.ok(dispenseService.returnAll(id, operatorId, operatorName, reason));
    }
}
