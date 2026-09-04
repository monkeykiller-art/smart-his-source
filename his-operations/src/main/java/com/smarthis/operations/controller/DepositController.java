package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.DepositQueryRequest;
import com.smarthis.operations.dto.request.DepositRefundRequest;
import com.smarthis.operations.dto.request.DepositRequest;
import com.smarthis.operations.dto.response.DepositAccountVo;
import com.smarthis.operations.dto.response.DepositVo;
import com.smarthis.operations.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operations/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PostMapping("/collect")
    public ApiResponse<DepositVo> collect(@Valid @RequestBody DepositRequest request) {
        return ApiResponse.ok(depositService.collect(request));
    }

    @PostMapping("/refund")
    public ApiResponse<DepositVo> refund(@Valid @RequestBody DepositRefundRequest request) {
        return ApiResponse.ok(depositService.refund(request));
    }

    @GetMapping("/admission/{admissionId}")
    public ApiResponse<List<DepositVo>> queryByAdmission(@PathVariable Long admissionId) {
        return ApiResponse.ok(depositService.queryByAdmission(admissionId));
    }

    @GetMapping("/balance/{admissionId}")
    public ApiResponse<DepositAccountVo> getBalance(@PathVariable Long admissionId) {
        return ApiResponse.ok(depositService.getBalance(admissionId));
    }

    @GetMapping
    public ApiResponse<PageResult<DepositVo>> query(DepositQueryRequest request) {
        return ApiResponse.ok(depositService.query(request));
    }
}
