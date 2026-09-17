package com.smarthis.pharma.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugInteractionCheckRequest;
import com.smarthis.pharma.dto.request.DrugInteractionCreateRequest;
import com.smarthis.pharma.dto.request.DrugInteractionQueryRequest;
import com.smarthis.pharma.dto.response.DrugInteractionVo;
import com.smarthis.pharma.service.DrugInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharma/drug-interactions")
@RequiredArgsConstructor
public class DrugInteractionController {

    private final DrugInteractionService drugInteractionService;

    @PostMapping
    public ApiResponse<DrugInteractionVo> create(@Valid @RequestBody DrugInteractionCreateRequest request) {
        return ApiResponse.ok(drugInteractionService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<DrugInteractionVo>> query(DrugInteractionQueryRequest request) {
        return ApiResponse.ok(drugInteractionService.query(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DrugInteractionVo> update(@PathVariable Long id,
                                                  @Valid @RequestBody DrugInteractionCreateRequest request) {
        return ApiResponse.ok(drugInteractionService.update(id, request));
    }

    @PostMapping("/check")
    public ApiResponse<List<DrugInteractionVo>> check(@Valid @RequestBody DrugInteractionCheckRequest request) {
        return ApiResponse.ok(drugInteractionService.check(request));
    }
}
