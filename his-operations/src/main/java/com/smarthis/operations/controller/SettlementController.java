package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.SettlementCancelRequest;
import com.smarthis.operations.dto.request.SettlementCreateRequest;
import com.smarthis.operations.dto.request.SettlementQueryRequest;
import com.smarthis.operations.dto.response.SettlementPreviewVo;
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
    public ApiResponse<SettlementVo> create(@Valid @RequestBody SettlementCreateRequest request) {
        return ApiResponse.ok(settlementService.create(request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SettlementVo> cancel(@PathVariable Long id, @Valid @RequestBody SettlementCancelRequest request) {
        return ApiResponse.ok(settlementService.cancel(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<SettlementVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(settlementService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<SettlementVo>> query(SettlementQueryRequest request) {
        return ApiResponse.ok(settlementService.query(request));
    }

    @GetMapping("/preview/{admissionId}")
    public ApiResponse<SettlementPreviewVo> preview(@PathVariable Long admissionId) {
        return ApiResponse.ok(settlementService.preview(admissionId));
    }
}
