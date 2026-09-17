package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugCatalogQueryRequest;
import com.smarthis.pharma.dto.request.DrugCatalogSaveRequest;
import com.smarthis.pharma.dto.response.DrugCatalogVo;
import com.smarthis.pharma.service.DrugCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharma/drugs")
@RequiredArgsConstructor
public class DrugCatalogController {
    private final DrugCatalogService drugCatalogService;

    @PostMapping
    public ApiResponse<DrugCatalogVo> create(@Valid @RequestBody DrugCatalogSaveRequest request) {
        return ApiResponse.ok(drugCatalogService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DrugCatalogVo> update(@PathVariable Long id, @Valid @RequestBody DrugCatalogSaveRequest request) {
        return ApiResponse.ok(drugCatalogService.update(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DrugCatalogVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(drugCatalogService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<DrugCatalogVo>> query(DrugCatalogQueryRequest request) {
        return ApiResponse.ok(drugCatalogService.query(request));
    }

    @PutMapping("/{id}/active")
    public ApiResponse<DrugCatalogVo> setActive(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.ok(drugCatalogService.setActive(id, active));
    }
}
