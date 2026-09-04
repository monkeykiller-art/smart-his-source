package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.DepositQueryRequest;
import com.smarthis.operations.dto.request.DepositCreateRequest;
import com.smarthis.operations.dto.request.DepositRefundRequest;
import com.smarthis.operations.dto.response.DepositVo;
import com.smarthis.operations.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    public ApiResponse<DepositVo> create(@Valid @RequestBody DepositCreateRequest request) {
        return ApiResponse.ok(depositService.create(request));
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<DepositVo> refund(@PathVariable Long id, @Valid @RequestBody DepositRefundRequest request) {
        return ApiResponse.ok(depositService.refund(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DepositVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(depositService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<DepositVo>> query(DepositQueryRequest request) {
        return ApiResponse.ok(depositService.query(request));
    }
}
