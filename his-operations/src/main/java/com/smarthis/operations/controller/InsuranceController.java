package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.operations.dto.response.SettlementPreviewVo;
import com.smarthis.operations.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Local insurance adapter facade. It exposes the same deterministic calculation used by settlement. */
@RestController
@RequestMapping("/api/operations/insurance")
@RequiredArgsConstructor
public class InsuranceController {
    private final SettlementService settlementService;

    @GetMapping("/preview/{admissionId}")
    public ApiResponse<SettlementPreviewVo> preview(@PathVariable Long admissionId) {
        return ApiResponse.ok(settlementService.preview(admissionId));
    }
}
