package com.smarthis.patient.client;

import com.smarthis.common.model.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "his-operations", contextId = "operationsClient")
public interface OperationsClient {

    @PostMapping("/api/operations/bills/registration")
    ApiResponse<Map<String, Object>> createRegistrationBill(@RequestBody Map<String, Object> request);
}
