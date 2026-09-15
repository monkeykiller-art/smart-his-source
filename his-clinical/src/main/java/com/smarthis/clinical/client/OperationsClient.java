package com.smarthis.clinical.client;

import com.smarthis.common.model.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "his-operations", contextId = "clinicalOperationsClient")
public interface OperationsClient {

    @PostMapping("/api/operations/bills/order")
    ApiResponse<Map<String, Object>> createOrderBill(@RequestBody Map<String, Object> request);

    @PostMapping("/api/operations/bills/order/void")
    ApiResponse<Map<String, Object>> voidOrderSource(@RequestBody Map<String, Object> request);
}
