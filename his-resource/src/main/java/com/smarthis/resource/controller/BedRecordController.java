package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.BedRecordCreateRequest;
import com.smarthis.resource.dto.response.BedOverviewVo;
import com.smarthis.resource.dto.response.BedRecordVo;
import com.smarthis.resource.service.BedRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BedRecordController {

    private final BedRecordService bedRecordService;

    @PostMapping("/api/resource/bed-records")
    public ApiResponse<BedRecordVo> admit(@Valid @RequestBody BedRecordCreateRequest request) {
        return ApiResponse.ok(bedRecordService.admit(request));
    }

    @GetMapping("/api/resource/bed-records/{id}")
    public ApiResponse<BedRecordVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(bedRecordService.getById(id));
    }

    @GetMapping("/api/resource/bed-records")
    public ApiResponse<PageResult<BedRecordVo>> query(
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) String recordStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(bedRecordService.query(wardId, recordStatus, page, size));
    }

    @PutMapping("/api/resource/bed-records/{id}/discharge")
    public ApiResponse<BedRecordVo> discharge(@PathVariable Long id) {
        return ApiResponse.ok(bedRecordService.discharge(id));
    }

    @GetMapping("/api/resource/bed-overview")
    public ApiResponse<List<BedOverviewVo>> bedOverview(@RequestParam(required = false) Long wardId) {
        return ApiResponse.ok(bedRecordService.bedOverview(wardId));
    }
}
