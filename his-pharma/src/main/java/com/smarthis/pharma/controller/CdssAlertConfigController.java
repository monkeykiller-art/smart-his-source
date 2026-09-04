package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.CdssAlertConfigCreateRequest;
import com.smarthis.pharma.dto.request.CdssAlertConfigQueryRequest;
import com.smarthis.pharma.dto.response.CdssAlertConfigVo;
import com.smarthis.pharma.service.CdssAlertConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharma/cdss-configs")
@RequiredArgsConstructor
public class CdssAlertConfigController {

    private final CdssAlertConfigService cdssAlertConfigService;

    @PostMapping
    public ApiResponse<CdssAlertConfigVo> create(@Valid @RequestBody CdssAlertConfigCreateRequest request) {
        return ApiResponse.ok(cdssAlertConfigService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CdssAlertConfigVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(cdssAlertConfigService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<CdssAlertConfigVo>> query(CdssAlertConfigQueryRequest request) {
        return ApiResponse.ok(cdssAlertConfigService.query(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CdssAlertConfigVo> update(@PathVariable Long id,
                                                  @Valid @RequestBody CdssAlertConfigCreateRequest request) {
        return ApiResponse.ok(cdssAlertConfigService.update(id, request));
    }

    @PutMapping("/{id}/toggle")
    public ApiResponse<CdssAlertConfigVo> toggle(@PathVariable Long id) {
        return ApiResponse.ok(cdssAlertConfigService.toggle(id));
    }
}
