package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.patient.dto.request.EncounterOpenRequest;
import com.smarthis.patient.dto.response.EncounterVo;
import com.smarthis.patient.service.EncounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/encounters")
@RequiredArgsConstructor
public class EncounterController {

    private final EncounterService encounterService;

    @PostMapping
    public ApiResponse<EncounterVo> open(@Valid @RequestBody EncounterOpenRequest request) {
        return ApiResponse.ok(encounterService.open(request.getRegId(), request.getChiefComplaint()));
    }

    @GetMapping("/{id}")
    public ApiResponse<EncounterVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(encounterService.getById(id));
    }

    @GetMapping("/by-reg/{regId}")
    public ApiResponse<EncounterVo> getByRegId(@PathVariable Long regId) {
        return ApiResponse.ok(encounterService.getByRegId(regId));
    }

    @PutMapping("/{id}/close")
    public ApiResponse<Void> close(@PathVariable Long id) {
        encounterService.close(id);
        return ApiResponse.ok();
    }

    @GetMapping("/patient/{patientId}")
    public ApiResponse<List<EncounterVo>> listByPatient(@PathVariable Long patientId) {
        return ApiResponse.ok(encounterService.listByPatient(patientId));
    }
}
