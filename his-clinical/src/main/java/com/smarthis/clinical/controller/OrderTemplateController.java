package com.smarthis.clinical.controller;

import com.smarthis.clinical.dto.response.OrderTemplateVo;
import com.smarthis.clinical.entity.OrderTemplate;
import com.smarthis.clinical.entity.OrderTemplateItem;
import com.smarthis.clinical.service.OrderTemplateService;
import com.smarthis.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinical/order-templates")
@RequiredArgsConstructor
public class OrderTemplateController {

    private final OrderTemplateService orderTemplateService;

    @PostMapping
    public ApiResponse<OrderTemplateVo> create(@RequestBody OrderTemplateCreateRequest request) {
        return ApiResponse.ok(orderTemplateService.create(request.getTemplate(), request.getItems()));
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderTemplateVo> update(@PathVariable Long id, @RequestBody OrderTemplateCreateRequest request) {
        return ApiResponse.ok(orderTemplateService.update(id, request.getTemplate(), request.getItems()));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderTemplateVo> getById(@PathVariable Long id) {
        return ApiResponse.ok(orderTemplateService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orderTemplateService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/dept/{deptId}")
    public ApiResponse<List<OrderTemplateVo>> listByDept(@PathVariable Long deptId) {
        return ApiResponse.ok(orderTemplateService.listByDept(deptId));
    }

    @lombok.Data
    public static class OrderTemplateCreateRequest {
        private OrderTemplate template;
        private List<OrderTemplateItem> items;
    }
}
