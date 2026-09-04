package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.patient.dto.response.DepartmentVo;
import com.smarthis.patient.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<DepartmentVo>> listAll() {
        return ApiResponse.ok(departmentService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(departmentService.getById(id));
    }
}
