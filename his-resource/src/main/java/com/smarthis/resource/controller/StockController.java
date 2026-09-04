package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.StockQueryRequest;
import com.smarthis.resource.dto.response.StockVo;
import com.smarthis.resource.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ApiResponse<PageResult<StockVo>> list(StockQueryRequest request) {
        return ApiResponse.ok(stockService.list(request));
    }

    @GetMapping("/drug")
    public ApiResponse<List<StockVo>> listByDrug(@RequestParam Long drugId, @RequestParam(required = false) Long pharmacyId) {
        return ApiResponse.ok(stockService.listByDrug(drugId, pharmacyId));
    }
}
