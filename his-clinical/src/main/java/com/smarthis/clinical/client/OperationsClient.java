package com.smarthis.clinical.client;

import com.smarthis.common.model.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "his-operations", contextId = "clinicalOperationsClient")
public interface OperationsClient {

    @PostMapping("/api/operations/bills/charge")
    ApiResponse<Map<String, Object>> chargeFeeItem(@RequestBody Map<String, Object> request);
}
