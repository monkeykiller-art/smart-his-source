package com.smarthis.emergency.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.security.RequiresPermission;
import com.smarthis.emergency.dto.request.EmergencyTriageCreateRequest;
import com.smarthis.emergency.entity.EmergencyTriage;
import com.smarthis.emergency.service.EmergencyTriageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency/triage")
@RequiredArgsConstructor
public class EmergencyTriageController {
    private final EmergencyTriageService service;

    @PostMapping
    @RequiresPermission("emergency:triage:create")
    public ApiResponse<EmergencyTriage> create(@Valid @RequestBody EmergencyTriageCreateRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("emergency:triage:update")
    public ApiResponse<EmergencyTriage> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.ok(service.updateStatus(id, status));
    }

    @GetMapping
    @RequiresPermission("emergency:triage:list")
    public ApiResponse<List<EmergencyTriage>> queue() { return ApiResponse.ok(service.queue()); }
}
