package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.BillChargeItemRequest;
import com.smarthis.operations.dto.request.BillChargeRequest;
import com.smarthis.operations.dto.request.BillCreateRequest;
import com.smarthis.operations.dto.request.BillPaymentRequest;
import com.smarthis.operations.dto.request.BillQueryRequest;
import com.smarthis.operations.dto.request.BillRegistrationRequest;
import com.smarthis.operations.dto.request.BillRefundRequest;
import com.smarthis.operations.dto.request.BillVoidRequest;
import com.smarthis.operations.dto.response.BillItemVo;
import com.smarthis.operations.dto.response.BillTransactionVo;
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

    @PostMapping("/order")
    public ApiResponse<BillVo> createOrderBill(@Valid @RequestBody com.smarthis.operations.dto.request.BillOrderRequest request) {
        return ApiResponse.ok(billService.createFromOrder(request));
    }

    @PostMapping("/order/void")
    public ApiResponse<BillVo> voidOrderSource(@Valid @RequestBody com.smarthis.operations.dto.request.BillOrderCancelRequest request) {
        return ApiResponse.ok(billService.voidOrderSource(request));
    }

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

    @GetMapping("/{id}/items")
    public ApiResponse<java.util.List<BillItemVo>> listItems(@PathVariable Long id) {
        return ApiResponse.ok(billService.listItems(id));
    }

    @GetMapping("/{id}/transactions")
    public ApiResponse<java.util.List<BillTransactionVo>> listTransactions(@PathVariable Long id) {
        return ApiResponse.ok(billService.listTransactions(id));
    }

    @PostMapping("/{id}/payments")
    public ApiResponse<BillTransactionVo> pay(@PathVariable Long id, @Valid @RequestBody BillPaymentRequest request) {
        return ApiResponse.ok(billService.pay(id, request));
    }

    @PostMapping("/{id}/refunds")
    public ApiResponse<BillTransactionVo> refund(@PathVariable Long id, @Valid @RequestBody BillRefundRequest request) {
        return ApiResponse.ok(billService.refund(id, request));
    }

    @PostMapping("/{id}/void")
    public ApiResponse<BillVo> voidBill(@PathVariable Long id, @Valid @RequestBody BillVoidRequest request) {
        return ApiResponse.ok(billService.voidBill(id, request));
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
