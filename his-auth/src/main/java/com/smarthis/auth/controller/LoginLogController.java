package com.smarthis.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.auth.entity.AuthLoginLog;
import com.smarthis.auth.mapper.LoginLogMapper;
import com.smarthis.auth.entity.AuthOperationLog;
import com.smarthis.auth.mapper.OperationLogMapper;
import com.smarthis.common.model.ApiResponse;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.security.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogMapper loginLogMapper;
    private final OperationLogMapper operationLogMapper;

    @GetMapping("/login")
    @RequiresPermission("auth:log:list")
    public ApiResponse<PageResult<AuthLoginLog>> listLoginLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String loginResult,
            PageQuery pageQuery) {
        LambdaQueryWrapper<AuthLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) {
            wrapper.like(AuthLoginLog::getUsername, username);
        }
        if (loginResult != null && !loginResult.isBlank()) {
            wrapper.eq(AuthLoginLog::getLoginResult, loginResult);
        }
        wrapper.orderByDesc(AuthLoginLog::getLoginTime);

        Page<AuthLoginLog> page = loginLogMapper.selectPage(pageQuery.toPage(), wrapper);
        PageResult<AuthLoginLog> result = new PageResult<>(
                page.getRecords(), page.getTotal(),
                pageQuery.getPage(), pageQuery.getSize());
        return ApiResponse.ok(result);
    }

    @GetMapping("/operations")
    @RequiresPermission("auth:log:list")
    public ApiResponse<PageResult<AuthOperationLog>> listOperationLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) Integer responseCode,
            PageQuery pageQuery) {
        LambdaQueryWrapper<AuthOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null && !username.isBlank(), AuthOperationLog::getUsername, username)
                .eq(operationType != null && !operationType.isBlank(), AuthOperationLog::getOperationType, operationType)
                .eq(responseCode != null, AuthOperationLog::getResponseCode, responseCode)
                .orderByDesc(AuthOperationLog::getOperationTime);
        Page<AuthOperationLog> page = operationLogMapper.selectPage(pageQuery.toPage(), wrapper);
        return ApiResponse.ok(new PageResult<>(page.getRecords(), page.getTotal(), pageQuery.getPage(), pageQuery.getSize()));
    }
}
