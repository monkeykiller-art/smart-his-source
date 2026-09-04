package com.smarthis.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.auth.entity.AuthLoginLog;
import com.smarthis.auth.mapper.LoginLogMapper;
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
}
