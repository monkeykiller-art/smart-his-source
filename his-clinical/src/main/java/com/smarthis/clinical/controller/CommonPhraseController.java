package com.smarthis.clinical.controller;

import com.smarthis.clinical.dto.request.CommonPhraseCreateRequest;
import com.smarthis.clinical.dto.request.CommonPhraseQueryRequest;
import com.smarthis.clinical.dto.request.CommonPhraseUpdateRequest;
import com.smarthis.clinical.dto.response.CommonPhraseVo;
import com.smarthis.clinical.service.CommonPhraseService;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical/common-phrases")
@RequiredArgsConstructor
public class CommonPhraseController {

    private final CommonPhraseService commonPhraseService;

    @PostMapping
    public ApiResponse<CommonPhraseVo> create(@Valid @RequestBody CommonPhraseCreateRequest request) {
        return ApiResponse.ok(commonPhraseService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CommonPhraseVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(commonPhraseService.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResult<CommonPhraseVo>> query(@Valid CommonPhraseQueryRequest request) {
        return ApiResponse.ok(commonPhraseService.query(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CommonPhraseVo> update(@PathVariable Long id, @Valid @RequestBody CommonPhraseUpdateRequest request) {
        return ApiResponse.ok(commonPhraseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        commonPhraseService.delete(id);
        return ApiResponse.ok();
    }
}
