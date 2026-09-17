package com.smarthis.patient.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.security.RequiresPermission;
import com.smarthis.patient.entity.InpatientBed;
import com.smarthis.patient.service.InpatientBedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/patient/inpatient-beds")
@RequiredArgsConstructor
public class InpatientBedController {
    private final InpatientBedService bedService;

    @GetMapping
    @RequiresPermission("resource:ward:list")
    public ApiResponse<List<InpatientBed>> list(@RequestParam(required = false) Long wardId,
                                                @RequestParam(required = false) String status) {
        return ApiResponse.ok(bedService.list(wardId, status));
    }
}
