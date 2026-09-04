package com.smarthis.clinical.controller;

import com.smarthis.clinical.dto.request.Icd10QueryRequest;
import com.smarthis.clinical.dto.response.Icd10Vo;
import com.smarthis.clinical.service.Icd10Service;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical/icd10")
@RequiredArgsConstructor
public class Icd10Controller {

    private final Icd10Service icd10Service;

    @GetMapping("/search")
    public ApiResponse<PageResult<Icd10Vo>> search(Icd10QueryRequest request) {
        return ApiResponse.ok(icd10Service.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Icd10Vo> getById(@PathVariable Long id) {
        return ApiResponse.ok(icd10Service.getById(id));
    }
}
