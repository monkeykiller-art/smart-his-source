package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.PharmacyCreateRequest;
import com.smarthis.resource.dto.request.PharmacyQueryRequest;
import com.smarthis.resource.dto.request.PharmacyUpdateRequest;
import com.smarthis.resource.dto.response.PharmacyVo;
import com.smarthis.resource.service.PharmacyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resource/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;

    @PostMapping
    public ApiResponse<PharmacyVo> create(@Valid @RequestBody PharmacyCreateRequest request) {
        return ApiResponse.ok(pharmacyService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<PharmacyVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(pharmacyService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<PharmacyVo> update(@PathVariable Long id, @Valid @RequestBody PharmacyUpdateRequest request) {
        return ApiResponse.ok(pharmacyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        pharmacyService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResult<PharmacyVo>> list(PharmacyQueryRequest request) {
        return ApiResponse.ok(pharmacyService.list(request));
    }
}
