package com.smarthis.emergency.controller;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.security.RequiresPermission;
import com.smarthis.emergency.dto.request.*;
import com.smarthis.emergency.entity.ObservationRecord;
import com.smarthis.emergency.entity.ResuscitationRecord;
import com.smarthis.emergency.service.EmergencyCareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/emergency") @RequiredArgsConstructor
public class EmergencyCareController {
    private final EmergencyCareService service;
    @PostMapping("/resuscitations") @RequiresPermission("emergency:resuscitation:create")
    public ApiResponse<ResuscitationRecord> start(@Valid @RequestBody ResuscitationCreateRequest request) { return ApiResponse.ok(service.startResuscitation(request)); }
    @PutMapping("/resuscitations/{id}/complete") @RequiresPermission("emergency:resuscitation:create")
    public ApiResponse<ResuscitationRecord> complete(@PathVariable Long id, @Valid @RequestBody ResuscitationCompleteRequest request) { return ApiResponse.ok(service.completeResuscitation(id, request)); }
    @GetMapping("/resuscitations") @RequiresPermission("emergency:triage:list")
    public ApiResponse<List<ResuscitationRecord>> resuscitations(@RequestParam(required = false) Long patientId) { return ApiResponse.ok(service.resuscitations(patientId)); }
    @PostMapping("/observations") @RequiresPermission("emergency:observation:manage")
    public ApiResponse<ObservationRecord> observe(@Valid @RequestBody ObservationCreateRequest request) { return ApiResponse.ok(service.admitObservation(request)); }
    @PutMapping("/observations/{id}/discharge") @RequiresPermission("emergency:observation:manage")
    public ApiResponse<ObservationRecord> discharge(@PathVariable Long id, @Valid @RequestBody ObservationDischargeRequest request) { return ApiResponse.ok(service.dischargeObservation(id, request)); }
    @GetMapping("/observations") @RequiresPermission("emergency:triage:list")
    public ApiResponse<List<ObservationRecord>> observations(@RequestParam(required = false) Long patientId) { return ApiResponse.ok(service.observations(patientId)); }
}
