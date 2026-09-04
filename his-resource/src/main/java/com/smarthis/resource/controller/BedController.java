package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.BedAdmitRequest;
import com.smarthis.resource.dto.request.BedCreateRequest;
import com.smarthis.resource.dto.request.BedDischargeRequest;
import com.smarthis.resource.dto.request.BedQueryRequest;
import com.smarthis.resource.dto.request.BedUpdateRequest;
import com.smarthis.resource.dto.response.BedOverviewVo;
import com.smarthis.resource.dto.response.BedRecordVo;
import com.smarthis.resource.dto.response.BedVo;
import com.smarthis.resource.service.BedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource/beds")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @PostMapping
    public ApiResponse<BedVo> create(@Valid @RequestBody BedCreateRequest request) {
        return ApiResponse.ok(bedService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BedVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(bedService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<BedVo> update(@PathVariable Long id, @Valid @RequestBody BedUpdateRequest request) {
        return ApiResponse.ok(bedService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bedService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResult<BedVo>> list(BedQueryRequest request) {
        return ApiResponse.ok(bedService.list(request));
    }

    @PostMapping("/admit")
    public ApiResponse<BedRecordVo> admit(@Valid @RequestBody BedAdmitRequest request) {
        return ApiResponse.ok(bedService.admit(request));
    }

    @PutMapping("/{id}/discharge")
    public ApiResponse<BedRecordVo> discharge(@PathVariable Long id, @RequestBody BedDischargeRequest request) {
        return ApiResponse.ok(bedService.discharge(id, request));
    }

    @GetMapping("/overview")
    public ApiResponse<List<BedOverviewVo>> overview(@RequestParam Long wardId) {
        return ApiResponse.ok(bedService.overview(wardId));
    }
}
