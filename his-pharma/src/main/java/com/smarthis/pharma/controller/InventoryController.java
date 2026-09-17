package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.pharma.dto.request.InventoryOperationRequest;
import com.smarthis.pharma.dto.response.InventoryBatchVo;
import com.smarthis.pharma.dto.response.InventoryTransactionVo;
import com.smarthis.pharma.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharma/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/operations")
    public ApiResponse<InventoryTransactionVo> operate(@Valid @RequestBody InventoryOperationRequest request) {
        return ApiResponse.ok(inventoryService.operate(request));
    }

    @GetMapping("/batches")
    public ApiResponse<List<InventoryBatchVo>> batches(@RequestParam(required = false) Long drugId,
                                                        @RequestParam(required = false) String warehouseCode,
                                                        @RequestParam(defaultValue = "false") boolean availableOnly) {
        return ApiResponse.ok(inventoryService.listBatches(drugId, warehouseCode, availableOnly));
    }

    @GetMapping("/near-expiry")
    public ApiResponse<List<InventoryBatchVo>> nearExpiry(@RequestParam(required = false) String warehouseCode,
                                                           @RequestParam(defaultValue = "90") int days) {
        return ApiResponse.ok(inventoryService.listNearExpiry(warehouseCode, days));
    }

    @GetMapping("/transactions")
    public ApiResponse<List<InventoryTransactionVo>> trace(@RequestParam(required = false) Long drugId,
                                                            @RequestParam(required = false) Long batchId) {
        return ApiResponse.ok(inventoryService.trace(drugId, batchId));
    }
}
