package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.resource.dto.request.StockMovementCreateRequest;
import com.smarthis.resource.dto.response.StockMovementVo;
import com.smarthis.resource.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    public ApiResponse<StockMovementVo> create(@Valid @RequestBody StockMovementCreateRequest request) {
        return ApiResponse.ok(stockMovementService.create(request));
    }

    @GetMapping("/drug")
    public ApiResponse<List<StockMovementVo>> listByDrug(@RequestParam Long drugId, @RequestParam(required = false) Long pharmacyId) {
        return ApiResponse.ok(stockMovementService.listByDrug(drugId, pharmacyId));
    }
}
