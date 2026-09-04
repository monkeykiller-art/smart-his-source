package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.WardCreateRequest;
import com.smarthis.resource.dto.request.WardQueryRequest;
import com.smarthis.resource.dto.request.WardUpdateRequest;
import com.smarthis.resource.dto.response.WardVo;
import com.smarthis.resource.service.WardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    @PostMapping
    public ApiResponse<WardVo> create(@Valid @RequestBody WardCreateRequest request) {
        return ApiResponse.ok(wardService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<WardVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(wardService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<WardVo> update(@PathVariable Long id, @Valid @RequestBody WardUpdateRequest request) {
        return ApiResponse.ok(wardService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        wardService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResult<WardVo>> list(WardQueryRequest request) {
        return ApiResponse.ok(wardService.list(request));
    }

    @GetMapping("/all")
    public ApiResponse<List<WardVo>> listAll() {
        return ApiResponse.ok(wardService.listAll());
    }
}
