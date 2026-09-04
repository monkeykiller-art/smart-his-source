package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.DispenseCreateRequest;
import com.smarthis.resource.dto.request.DispenseQueryRequest;
import com.smarthis.resource.dto.response.DispenseVo;
import com.smarthis.resource.service.DispenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resource/dispenses")
@RequiredArgsConstructor
public class DispenseController {

    private final DispenseService dispenseService;

    @PostMapping
    public ApiResponse<DispenseVo> create(@Valid @RequestBody DispenseCreateRequest request) {
        return ApiResponse.ok(dispenseService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DispenseVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(dispenseService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<DispenseVo>> list(DispenseQueryRequest request) {
        return ApiResponse.ok(dispenseService.list(request));
    }

    @PutMapping("/{id}/review")
    public ApiResponse<DispenseVo> review(@PathVariable Long id, @RequestParam String reviewerId) {
        return ApiResponse.ok(dispenseService.review(id, reviewerId));
    }
}
