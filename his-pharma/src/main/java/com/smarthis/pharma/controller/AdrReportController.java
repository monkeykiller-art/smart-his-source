package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.AdrReportCreateRequest;
import com.smarthis.pharma.dto.request.AdrReportQueryRequest;
import com.smarthis.pharma.dto.request.AdrReportReviewRequest;
import com.smarthis.pharma.dto.response.AdrReportVo;
import com.smarthis.pharma.service.AdrReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharma/adr-reports")
@RequiredArgsConstructor
public class AdrReportController {

    private final AdrReportService adrReportService;

    @PostMapping
    public ApiResponse<AdrReportVo> create(@Valid @RequestBody AdrReportCreateRequest request) {
        return ApiResponse.ok(adrReportService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdrReportVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(adrReportService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<AdrReportVo>> query(AdrReportQueryRequest request) {
        return ApiResponse.ok(adrReportService.query(request));
    }

    @PutMapping("/{id}/review")
    public ApiResponse<AdrReportVo> review(@PathVariable Long id,
                                            @Valid @RequestBody AdrReportReviewRequest request) {
        return ApiResponse.ok(adrReportService.review(id, request));
    }

    @PutMapping("/{id}/report")
    public ApiResponse<AdrReportVo> reportToAuthority(@PathVariable Long id) {
        return ApiResponse.ok(adrReportService.reportToAuthority(id));
    }
}
