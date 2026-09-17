package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugAllergyCrossCreateRequest;
import com.smarthis.pharma.dto.request.DrugAllergyCrossQueryRequest;
import com.smarthis.pharma.dto.response.DrugAllergyCrossVo;
import com.smarthis.pharma.service.DrugAllergyCrossService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharma/allergy-cross")
@RequiredArgsConstructor
public class DrugAllergyCrossController {

    private final DrugAllergyCrossService drugAllergyCrossService;

    @PostMapping
    public ApiResponse<DrugAllergyCrossVo> create(@Valid @RequestBody DrugAllergyCrossCreateRequest request) {
        return ApiResponse.ok(drugAllergyCrossService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<DrugAllergyCrossVo>> query(DrugAllergyCrossQueryRequest request) {
        return ApiResponse.ok(drugAllergyCrossService.query(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DrugAllergyCrossVo> update(@PathVariable Long id,
                                                   @Valid @RequestBody DrugAllergyCrossCreateRequest request) {
        return ApiResponse.ok(drugAllergyCrossService.update(id, request));
    }
}
