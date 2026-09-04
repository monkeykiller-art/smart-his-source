package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.patient.dto.request.ScheduleGenerateRequest;
import com.smarthis.patient.dto.request.ScheduleQueryRequest;
import com.smarthis.patient.dto.response.ScheduleVo;
import com.smarthis.patient.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/patient/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generate(@Valid @RequestBody ScheduleGenerateRequest request) {
        int count = scheduleService.generate(request);
        return ApiResponse.ok(Map.of("generated", count));
    }

    @GetMapping
    public ApiResponse<PageResult<ScheduleVo>> query(ScheduleQueryRequest request) {
        return ApiResponse.ok(scheduleService.query(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ScheduleVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(scheduleService.getById(id));
    }

    @PutMapping("/{id}/stop")
    public ApiResponse<Void> stop(@PathVariable Long id) {
        scheduleService.stop(id);
        return ApiResponse.ok();
    }
}
