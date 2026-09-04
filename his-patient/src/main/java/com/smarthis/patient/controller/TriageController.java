package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.patient.dto.request.TriageCallNextRequest;
import com.smarthis.patient.dto.response.TriageVo;
import com.smarthis.patient.service.TriageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient/triage")
@RequiredArgsConstructor
public class TriageController {

    private final TriageService triageService;

    @PostMapping("/enqueue/{regId}")
    public ApiResponse<TriageVo> enqueue(@PathVariable Long regId) {
        return ApiResponse.ok(triageService.enqueue(regId));
    }

    @PostMapping("/call-next")
    public ApiResponse<TriageVo> callNext(@Valid @RequestBody TriageCallNextRequest request) {
        return ApiResponse.ok(triageService.callNext(request.getDeptId(), request.getDoctorId()));
    }

    @PutMapping("/{id}/finish")
    public ApiResponse<TriageVo> finish(@PathVariable Long id) {
        return ApiResponse.ok(triageService.finish(id));
    }

    @GetMapping("/queue")
    public ApiResponse<List<TriageVo>> queue(@RequestParam Long deptId,
                                             @RequestParam(required = false) Long doctorId) {
        return ApiResponse.ok(triageService.queueByDoctor(deptId, doctorId));
    }

    @GetMapping("/waiting-count")
    public ApiResponse<Map<String, Object>> waitingCount(@RequestParam Long deptId,
                                                         @RequestParam(required = false) Long doctorId) {
        int count = triageService.waitingCount(deptId, doctorId);
        return ApiResponse.ok(Map.of("count", count));
    }
}
