package com.smarthis.operations.controller;

import com.smarthis.common.model.ApiResponse;
import com.smarthis.operations.dto.response.FeeTemplateItemVo;
import com.smarthis.operations.dto.response.FeeTemplateVo;
import com.smarthis.operations.service.FeeTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/operations/fee-templates")
@RequiredArgsConstructor
public class FeeTemplateController {

    private final FeeTemplateService feeTemplateService;

    @GetMapping("/{id}")
    public ApiResponse<FeeTemplateVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(feeTemplateService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<FeeTemplateVo>> listByDept(@RequestParam(required = false) Long deptId) {
        return ApiResponse.ok(feeTemplateService.listByDept(deptId));
    }

    @PostMapping
    public ApiResponse<FeeTemplateVo> create(
            @RequestParam String templateName,
            @RequestParam(required = false) String templateCategory,
            @RequestParam(required = false, defaultValue = "DEPARTMENT") String templateLevel,
            @RequestParam(required = false) Long deptId) {
        return ApiResponse.ok(feeTemplateService.create(templateName, templateCategory, templateLevel, deptId));
    }

    @PutMapping("/{id}")
    public ApiResponse<FeeTemplateVo> update(
            @PathVariable Long id,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String templateCategory,
            @RequestParam(required = false) Integer sortOrder) {
        return ApiResponse.ok(feeTemplateService.update(id, templateName, templateCategory, sortOrder));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        feeTemplateService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{templateId}/items")
    public ApiResponse<FeeTemplateItemVo> addItem(
            @PathVariable Long templateId,
            @RequestParam(required = false, defaultValue = "FEE") String itemType,
            @RequestParam(required = false) String itemCode,
            @RequestParam String itemName,
            @RequestParam(required = false) BigDecimal quantity,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) Long executeDeptId,
            @RequestParam(required = false) Integer itemSeq) {
        return ApiResponse.ok(feeTemplateService.addItem(templateId, itemType, itemCode, itemName,
                quantity, unit, executeDeptId, itemSeq));
    }

    @DeleteMapping("/{templateId}/items/{itemId}")
    public ApiResponse<Void> removeItem(@PathVariable Long templateId, @PathVariable Long itemId) {
        feeTemplateService.removeItem(templateId, itemId);
        return ApiResponse.ok();
    }
}
