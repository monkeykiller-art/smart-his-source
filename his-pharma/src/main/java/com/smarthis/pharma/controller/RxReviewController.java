package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.RxReviewCreateRequest;
import com.smarthis.pharma.dto.request.RxReviewQueryRequest;
import com.smarthis.pharma.dto.request.RxReviewRejectRequest;
import com.smarthis.pharma.dto.response.RxReviewVo;
import com.smarthis.pharma.service.RxReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharma/rx-reviews")
@RequiredArgsConstructor
public class RxReviewController {

    private final RxReviewService rxReviewService;

    @PostMapping
    public ApiResponse<RxReviewVo> create(@Valid @RequestBody RxReviewCreateRequest request) {
        return ApiResponse.ok(rxReviewService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<RxReviewVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(rxReviewService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<RxReviewVo>> query(RxReviewQueryRequest request) {
        return ApiResponse.ok(rxReviewService.query(request));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<RxReviewVo> approve(@PathVariable Long id,
                                           @RequestParam(required = false) String reviewerId,
                                           @RequestParam(required = false) String reviewerName) {
        return ApiResponse.ok(rxReviewService.approve(id, reviewerId, reviewerName));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<RxReviewVo> reject(@PathVariable Long id,
                                          @RequestParam(required = false) String reviewerId,
                                          @RequestParam(required = false) String reviewerName,
                                          @Valid @RequestBody RxReviewRejectRequest request) {
        return ApiResponse.ok(rxReviewService.reject(id, reviewerId, reviewerName, request.getRejectReason()));
    }
}
