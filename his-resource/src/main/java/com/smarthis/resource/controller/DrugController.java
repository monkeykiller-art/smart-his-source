package com.smarthis.resource.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.DrugCreateRequest;
import com.smarthis.resource.dto.request.DrugQueryRequest;
import com.smarthis.resource.dto.request.DrugUpdateRequest;
import com.smarthis.resource.dto.response.DrugVo;
import com.smarthis.resource.service.DrugService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    @PostMapping
    public ApiResponse<DrugVo> create(@Valid @RequestBody DrugCreateRequest request) {
        return ApiResponse.ok(drugService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DrugVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(drugService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<DrugVo> update(@PathVariable Long id, @Valid @RequestBody DrugUpdateRequest request) {
        return ApiResponse.ok(drugService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        drugService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResult<DrugVo>> list(DrugQueryRequest request) {
        return ApiResponse.ok(drugService.list(request));
    }

    @GetMapping("/search")
    public ApiResponse<List<DrugVo>> search(@RequestParam String keyword) {
        return ApiResponse.ok(drugService.search(keyword));
    }
}
