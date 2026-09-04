package com.smarthis.auth.controller;

import com.smarthis.auth.aspect.OperationLog;
import com.smarthis.auth.entity.AuthRole;
import com.smarthis.auth.service.RoleService;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.security.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @RequiresPermission("auth:role:create")
    @OperationLog(module = "auth", action = "CREATE_ROLE")
    public ApiResponse<AuthRole> createRole(@RequestBody AuthRole role) {
        return ApiResponse.ok(roleService.createRole(role));
    }

    @GetMapping("/{id}")
    @RequiresPermission("auth:role:read")
    public ApiResponse<AuthRole> getRole(@PathVariable Long id) {
        return ApiResponse.ok(roleService.getRoleById(id));
    }

    @GetMapping
    @RequiresPermission("auth:role:list")
    public ApiResponse<PageResult<AuthRole>> listRoles(
            @RequestParam(required = false) String keyword,
            PageQuery pageQuery) {
        return ApiResponse.ok(roleService.listRoles(keyword, pageQuery));
    }

    @PutMapping("/{roleId}/permissions")
    @RequiresPermission("auth:role:assign-permission")
    @OperationLog(module = "auth", action = "ASSIGN_PERMISSIONS")
    public ApiResponse<Void> assignPermissions(@PathVariable Long roleId,
                                               @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(roleId, permissionIds);
        return ApiResponse.ok();
    }
}
