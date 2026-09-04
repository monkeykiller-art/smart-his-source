package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugContraindicationCreateRequest;
import com.smarthis.pharma.dto.request.DrugContraindicationQueryRequest;
import com.smarthis.pharma.dto.response.DrugContraindicationVo;
import com.smarthis.pharma.service.DrugContraindicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharma/contraindications")
@RequiredArgsConstructor
public class DrugContraindicationController {

    private final DrugContraindicationService drugContraindicationService;

    @PostMapping
    public ApiResponse<DrugContraindicationVo> create(@Valid @RequestBody DrugContraindicationCreateRequest request) {
        return ApiResponse.ok(drugContraindicationService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<DrugContraindicationVo>> query(DrugContraindicationQueryRequest request) {
        return ApiResponse.ok(drugContraindicationService.query(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DrugContraindicationVo> update(@PathVariable Long id,
                                                       @Valid @RequestBody DrugContraindicationCreateRequest request) {
        return ApiResponse.ok(drugContraindicationService.update(id, request));
    }
}
