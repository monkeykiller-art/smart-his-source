package com.smarthis.patient.client;

import com.smarthis.common.model.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "his-operations", contextId = "operationsClient")
public interface OperationsClient {

    @PostMapping("/api/operations/bills/registration")
    ApiResponse<Map<String, Object>> createRegistrationBill(@RequestBody Map<String, Object> request);

    @GetMapping("/api/operations/bills/{id}")
    ApiResponse<Map<String, Object>> getBill(@PathVariable("id") Long id);

    @GetMapping("/api/operations/bills/{id}/transactions")
    ApiResponse<java.util.List<Map<String, Object>>> listTransactions(@PathVariable("id") Long id);

    @PostMapping("/api/operations/bills/{id}/payments")
    ApiResponse<Map<String, Object>> payBill(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/api/operations/bills/{id}/refunds")
    ApiResponse<Map<String, Object>> refundBill(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @PostMapping("/api/operations/bills/{id}/void")
    ApiResponse<Map<String, Object>> voidBill(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);
}
