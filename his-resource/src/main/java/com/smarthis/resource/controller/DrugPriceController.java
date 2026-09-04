package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.resource.dto.request.DrugPriceCreateRequest;
import com.smarthis.resource.dto.response.DrugPriceVo;
import com.smarthis.resource.service.DrugPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource/drug-prices")
@RequiredArgsConstructor
public class DrugPriceController {

    private final DrugPriceService drugPriceService;

    @PostMapping
    public ApiResponse<DrugPriceVo> create(@Valid @RequestBody DrugPriceCreateRequest request) {
        return ApiResponse.ok(drugPriceService.create(request));
    }

    @GetMapping("/drug/{drugId}")
    public ApiResponse<List<DrugPriceVo>> listByDrug(@PathVariable Long drugId) {
        return ApiResponse.ok(drugPriceService.listByDrug(drugId));
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    public ApiResponse<List<DrugPriceVo>> listByPharmacy(@PathVariable Long pharmacyId) {
        return ApiResponse.ok(drugPriceService.listByPharmacy(pharmacyId));
    }
}
