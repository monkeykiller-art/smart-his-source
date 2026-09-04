package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.SupplierCreateRequest;
import com.smarthis.resource.dto.request.SupplierQueryRequest;
import com.smarthis.resource.dto.request.SupplierUpdateRequest;
import com.smarthis.resource.dto.response.SupplierVo;
import com.smarthis.resource.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resource/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ApiResponse<SupplierVo> create(@Valid @RequestBody SupplierCreateRequest request) {
        return ApiResponse.ok(supplierService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<SupplierVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(supplierService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<SupplierVo> update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateRequest request) {
        return ApiResponse.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResult<SupplierVo>> list(SupplierQueryRequest request) {
        return ApiResponse.ok(supplierService.list(request));
    }
}
