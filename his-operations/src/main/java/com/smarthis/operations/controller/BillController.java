package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.BillChargeItemRequest;
import com.smarthis.operations.dto.request.BillChargeRequest;
import com.smarthis.operations.dto.request.BillCreateRequest;
import com.smarthis.operations.dto.request.BillQueryRequest;
import com.smarthis.operations.dto.request.BillRegistrationRequest;
import com.smarthis.operations.dto.response.BillItemVo;
import com.smarthis.operations.dto.response.BillVo;
import com.smarthis.operations.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ApiResponse<BillVo> create(@Valid @RequestBody BillCreateRequest request) {
        return ApiResponse.ok(billService.create(request));
    }

    @PostMapping("/registration")
    public ApiResponse<BillVo> createRegistrationBill(@Valid @RequestBody BillRegistrationRequest request) {
        return ApiResponse.ok(billService.createFromRegistration(request));
    }

    @PostMapping("/{id}/items")
    public ApiResponse<java.util.List<BillItemVo>> addChargeItem(
            @PathVariable Long id, @Valid @RequestBody BillChargeItemRequest request) {
        return ApiResponse.ok(billService.addChargeItem(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BillVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(billService.getById(id));
    }

    @PostMapping("/charge")
    public ApiResponse<java.util.List<BillItemVo>> charge(@Valid @RequestBody BillChargeRequest request) {
        return ApiResponse.ok(billService.chargeItems(request));
    }

    @GetMapping
    public ApiResponse<PageResult<BillVo>> query(BillQueryRequest request) {
        return ApiResponse.ok(billService.query(request));
    }
}
