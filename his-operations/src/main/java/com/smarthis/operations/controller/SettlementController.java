package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.SettlementQueryRequest;
import com.smarthis.operations.dto.request.SettlementRequest;
import com.smarthis.operations.dto.response.SettlementVo;
import com.smarthis.operations.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    public ApiResponse<SettlementVo> create(@Valid @RequestBody SettlementRequest request) {
        return ApiResponse.ok(settlementService.create(request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SettlementVo> cancel(@PathVariable Long id, @RequestParam String reason) {
        return ApiResponse.ok(settlementService.cancel(id, reason));
    }

    @GetMapping("/{id}")
    public ApiResponse<SettlementVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(settlementService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<SettlementVo>> query(SettlementQueryRequest request) {
        return ApiResponse.ok(settlementService.query(request));
    }
}
