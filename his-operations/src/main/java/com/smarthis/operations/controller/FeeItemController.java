package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.FeeItemQueryRequest;
import com.smarthis.operations.dto.response.FeeItemVo;
import com.smarthis.operations.service.FeeItemService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/fee-items")
@RequiredArgsConstructor
public class FeeItemController {

    private final FeeItemService feeItemService;

    @PostMapping
    public ApiResponse<FeeItemVo> create(@Valid @RequestBody com.smarthis.operations.dto.request.FeeItemCreateRequest request) {
        return ApiResponse.ok(feeItemService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<FeeItemVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(feeItemService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<FeeItemVo> update(@PathVariable Long id, @RequestBody com.smarthis.operations.dto.request.FeeItemUpdateRequest request) {
        return ApiResponse.ok(feeItemService.update(id, request));
    }

    @GetMapping
    public ApiResponse<PageResult<FeeItemVo>> query(FeeItemQueryRequest request) {
        return ApiResponse.ok(feeItemService.query(request));
    }
}
