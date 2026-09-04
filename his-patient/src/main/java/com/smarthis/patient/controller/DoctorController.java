package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.patient.dto.response.DoctorVo;
import com.smarthis.patient.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ApiResponse<List<DoctorVo>> listByDept(@RequestParam(required = false) Long deptId) {
        return ApiResponse.ok(doctorService.listByDept(deptId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DoctorVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(doctorService.getById(id));
    }
}
