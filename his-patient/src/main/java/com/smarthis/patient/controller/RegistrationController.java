package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.patient.dto.request.RegistrationCreateRequest;
import com.smarthis.patient.dto.request.RegistrationQueryRequest;
import com.smarthis.patient.dto.response.RegistrationVo;
import com.smarthis.patient.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ApiResponse<RegistrationVo> create(@Valid @RequestBody RegistrationCreateRequest request) {
        return ApiResponse.ok(registrationService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<RegistrationVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(registrationService.getById(id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        registrationService.cancel(id, reason);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<Void> markPaid(@PathVariable Long id, @RequestParam(required = false) Long billId) {
        registrationService.markPaid(id, billId);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/refund")
    public ApiResponse<Void> refund(@PathVariable Long id, @RequestParam(required = false) String reason) {
        registrationService.refund(id, reason);
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResult<RegistrationVo>> query(RegistrationQueryRequest request) {
        return ApiResponse.ok(registrationService.query(request));
    }
}
