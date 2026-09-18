package com.smarthis.clinical.controller;

import com.smarthis.clinical.dto.request.RecordTemplateCreateRequest;
import com.smarthis.clinical.dto.request.RecordTemplateQueryRequest;
import com.smarthis.clinical.dto.request.RecordTemplateUpdateRequest;
import com.smarthis.clinical.dto.response.RecordTemplateVo;
import com.smarthis.clinical.service.RecordTemplateService;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical/record-templates")
@RequiredArgsConstructor
public class RecordTemplateController {

    private final RecordTemplateService recordTemplateService;

    @PostMapping
    public ApiResponse<RecordTemplateVo> create(@Valid @RequestBody RecordTemplateCreateRequest request) {
        return ApiResponse.ok(recordTemplateService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<RecordTemplateVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(recordTemplateService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<RecordTemplateVo>> query(@Valid RecordTemplateQueryRequest request) {
        return ApiResponse.ok(recordTemplateService.query(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RecordTemplateVo> update(@PathVariable Long id, @Valid @RequestBody RecordTemplateUpdateRequest request) {
        return ApiResponse.ok(recordTemplateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        recordTemplateService.delete(id);
        return ApiResponse.ok();
    }
}
