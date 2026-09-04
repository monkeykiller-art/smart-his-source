package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.BillQueryRequest;
import com.smarthis.operations.dto.request.ChargeRequest;
import com.smarthis.operations.dto.request.RefundRequest;
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

    @PostMapping("/charge")
    public ApiResponse<BillVo> charge(@Valid @RequestBody ChargeRequest request) {
        return ApiResponse.ok(billService.charge(request));
    }

    @PostMapping("/refund")
    public ApiResponse<BillVo> refund(@Valid @RequestBody RefundRequest request) {
        return ApiResponse.ok(billService.refund(request));
    }

    @PostMapping("/registration")
    public ApiResponse<BillVo> createRegistrationBill(
            @RequestParam Long patientId,
            @RequestParam Long encounterId,
            @RequestParam Long deptId,
            @RequestParam Long feeItemId,
            @RequestParam(required = false, defaultValue = "OUTPATIENT") String visitType) {
        return ApiResponse.ok(billService.createRegistrationBill(patientId, encounterId, deptId, feeItemId, visitType));
    }

    @GetMapping("/{id}")
    public ApiResponse<BillVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(billService.getById(id));
    }

    @GetMapping("/billno/{billNo}")
    public ApiResponse<BillVo> getByBillNo(@PathVariable String billNo) {
        return ApiResponse.ok(billService.getByBillNo(billNo));
    }

    @GetMapping
    public ApiResponse<PageResult<BillVo>> query(BillQueryRequest request) {
        return ApiResponse.ok(billService.query(request));
    }
}
