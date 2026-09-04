package com.smarthis.auth.controller;

import com.smarthis.auth.aspect.OperationLog;
import com.smarthis.auth.dto.request.ChangePasswordRequest;
import com.smarthis.auth.dto.request.CreateUserRequest;
import com.smarthis.auth.dto.response.UserVo;
import com.smarthis.auth.service.UserService;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.security.RequiresPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @RequiresPermission("auth:user:create")
    @OperationLog(module = "auth", action = "CREATE_USER")
    public ApiResponse<UserVo> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.createUser(request));
    }

    @GetMapping("/{id}")
    @RequiresPermission("auth:user:read")
    public ApiResponse<UserVo> getUser(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUserById(id));
    }

    @GetMapping("/by-username/{username}")
    @RequiresPermission("auth:user:read")
    public ApiResponse<UserVo> getByUsername(@PathVariable String username) {
        return ApiResponse.ok(userService.getUserByUsername(username));
    }

    @GetMapping
    @RequiresPermission("auth:user:list")
    public ApiResponse<PageResult<UserVo>> listUsers(
            @RequestParam(required = false) String keyword,
            PageQuery pageQuery) {
        return ApiResponse.ok(userService.listUsers(keyword, pageQuery));
    }

    @PutMapping("/{userId}/roles")
    @RequiresPermission("auth:user:assign-role")
    @OperationLog(module = "auth", action = "ASSIGN_ROLES")
    public ApiResponse<Void> assignRoles(@PathVariable Long userId,
                                         @RequestBody List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
        return ApiResponse.ok();
    }

    @PutMapping("/{userId}/password")
    @RequiresPermission("auth:user:change-password")
    @OperationLog(module = "auth", action = "CHANGE_PASSWORD")
    public ApiResponse<Void> changePassword(@PathVariable Long userId,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.ok();
    }

    @PutMapping("/{userId}/reset-password")
    @RequiresPermission("auth:user:reset-password")
    @OperationLog(module = "auth", action = "RESET_PASSWORD")
    public ApiResponse<Void> resetPassword(@PathVariable Long userId,
                                           @RequestBody Map<String, String> body) {
        userService.resetPassword(userId, body.get("newPassword"));
        return ApiResponse.ok();
    }

    @PutMapping("/{userId}/lock")
    @RequiresPermission("auth:user:lock")
    @OperationLog(module = "auth", action = "LOCK_USER")
    public ApiResponse<Void> lockUser(@PathVariable Long userId) {
        userService.lockUser(userId);
        return ApiResponse.ok();
    }

    @PutMapping("/{userId}/unlock")
    @RequiresPermission("auth:user:unlock")
    @OperationLog(module = "auth", action = "UNLOCK_USER")
    public ApiResponse<Void> unlockUser(@PathVariable Long userId) {
        userService.unlockUser(userId);
        return ApiResponse.ok();
    }
}
