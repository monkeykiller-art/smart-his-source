package com.smarthis.auth.controller;

import com.smarthis.auth.aspect.OperationLog;
import com.smarthis.auth.entity.AuthPermission;
import com.smarthis.auth.service.PermissionService;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.security.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @RequiresPermission("auth:permission:create")
    @OperationLog(module = "auth", action = "CREATE_PERMISSION")
    public ApiResponse<AuthPermission> createPermission(@RequestBody AuthPermission permission) {
        return ApiResponse.ok(permissionService.createPermission(permission));
    }

    @GetMapping("/{id}")
    @RequiresPermission("auth:permission:read")
    public ApiResponse<AuthPermission> getPermission(@PathVariable Long id) {
        return ApiResponse.ok(permissionService.getPermissionById(id));
    }

    @GetMapping
    @RequiresPermission("auth:permission:list")
    public ApiResponse<PageResult<AuthPermission>> listPermissions(
            @RequestParam(required = false) String moduleCode,
            PageQuery pageQuery) {
        return ApiResponse.ok(permissionService.listPermissions(moduleCode, pageQuery));
    }

    @GetMapping("/module/{moduleCode}")
    @RequiresPermission("auth:permission:list")
    public ApiResponse<List<AuthPermission>> listByModule(@PathVariable String moduleCode) {
        return ApiResponse.ok(permissionService.listByModule(moduleCode));
    }

    @GetMapping("/tree")
    @RequiresPermission("auth:permission:list")
    public ApiResponse<List<AuthPermission>> listTree() {
        return ApiResponse.ok(permissionService.listTree());
    }
}
