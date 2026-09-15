package com.smarthis.clinical.controller;

import com.smarthis.clinical.dto.request.OrderCancelRequest;
import com.smarthis.clinical.dto.request.OrderCreateRequest;
import com.smarthis.clinical.dto.request.OrderVerifyRequest;
import com.smarthis.clinical.dto.response.OrderVo;
import com.smarthis.clinical.service.OrderService;
import com.smarthis.common.model.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinical/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final com.smarthis.clinical.service.impl.OrderBillingService billingService;

    @PutMapping("/{id}/submit")
    public ApiResponse<OrderVo> submit(@PathVariable Long id) {
        orderService.submit(id);
        billingService.sync(id);
        return ApiResponse.ok(orderService.getById(id));
    }

    @PostMapping
    public ApiResponse<OrderVo> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.ok(orderService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getById(id));
    }

    @PutMapping("/{id}/verify")
    public ApiResponse<Void> verify(@PathVariable Long id, @Valid @RequestBody OrderVerifyRequest request) {
        orderService.verify(id, request);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @Valid @RequestBody OrderCancelRequest request) {
        orderService.cancel(id, request);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/stop")
    public ApiResponse<Void> stop(@PathVariable Long id) {
        orderService.stop(id);
        return ApiResponse.ok();
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<List<OrderVo>> listByPatient(@PathVariable Long patientId) {
        return ApiResponse.ok(orderService.listByPatient(patientId));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<List<OrderVo>> listByAdmission(@PathVariable Long admissionId) {
        return ApiResponse.ok(orderService.listByAdmission(admissionId));
    }
}
