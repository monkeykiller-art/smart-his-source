package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.FeeItemQueryRequest;
import com.smarthis.operations.dto.response.FeeItemVo;
import com.smarthis.operations.service.FeeItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/fee-items")
@RequiredArgsConstructor
public class FeeItemController {

    private final FeeItemService feeItemService;

    @GetMapping("/{id}")
    public ApiResponse<FeeItemVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(feeItemService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<FeeItemVo>> query(FeeItemQueryRequest request) {
        return ApiResponse.ok(feeItemService.query(request));
    }
}
