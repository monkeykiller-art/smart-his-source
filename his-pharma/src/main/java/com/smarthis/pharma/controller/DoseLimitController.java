package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DoseLimitCreateRequest;
import com.smarthis.pharma.dto.request.DoseLimitQueryRequest;
import com.smarthis.pharma.dto.response.DoseLimitVo;
import com.smarthis.pharma.service.DoseLimitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharma/dose-limits")
@RequiredArgsConstructor
public class DoseLimitController {

    private final DoseLimitService doseLimitService;

    @PostMapping
    public ApiResponse<DoseLimitVo> create(@Valid @RequestBody DoseLimitCreateRequest request) {
        return ApiResponse.ok(doseLimitService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<DoseLimitVo>> query(DoseLimitQueryRequest request) {
        return ApiResponse.ok(doseLimitService.query(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DoseLimitVo> update(@PathVariable Long id,
                                            @Valid @RequestBody DoseLimitCreateRequest request) {
        return ApiResponse.ok(doseLimitService.update(id, request));
    }
}
