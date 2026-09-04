package com.smarthis.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.auth.entity.AuthPermission;
import com.smarthis.auth.mapper.PermissionMapper;
import com.smarthis.auth.service.PermissionService;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public AuthPermission createPermission(AuthPermission permission) {
        Long count = permissionMapper.selectCount(
                new LambdaQueryWrapper<AuthPermission>()
                        .eq(AuthPermission::getPermCode, permission.getPermCode()));
        if (count > 0) {
            throw new BusinessException(400, "permission code already exists");
        }
        if (permission.getPermStatus() == null) {
            permission.setPermStatus("ACTIVE");
        }
        if (permission.getSortOrder() == null) {
            permission.setSortOrder(0);
        }
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    public AuthPermission getPermissionById(Long id) {
        AuthPermission perm = permissionMapper.selectById(id);
        if (perm == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return perm;
    }

    @Override
    public PageResult<AuthPermission> listPermissions(String moduleCode, PageQuery pageQuery) {
        LambdaQueryWrapper<AuthPermission> wrapper = new LambdaQueryWrapper<>();
        if (moduleCode != null && !moduleCode.isBlank()) {
            wrapper.eq(AuthPermission::getModuleCode, moduleCode);
        }
        wrapper.orderByAsc(AuthPermission::getSortOrder);

        Page<AuthPermission> page = permissionMapper.selectPage(pageQuery.toPage(), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                pageQuery.getPage(), pageQuery.getSize());
    }

    @Override
    public List<AuthPermission> listByModule(String moduleCode) {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<AuthPermission>()
                        .eq(AuthPermission::getModuleCode, moduleCode)
                        .eq(AuthPermission::getPermStatus, "ACTIVE")
                        .orderByAsc(AuthPermission::getSortOrder));
    }

    @Override
    public List<AuthPermission> listTree() {
        List<AuthPermission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<AuthPermission>()
                        .eq(AuthPermission::getPermStatus, "ACTIVE")
                        .orderByAsc(AuthPermission::getSortOrder));

        Map<Long, List<AuthPermission>> childrenMap = all.stream()
                .filter(p -> p.getParentId() != null && p.getParentId() != 0)
                .collect(Collectors.groupingBy(AuthPermission::getParentId));

        List<AuthPermission> roots = all.stream()
                .filter(p -> p.getParentId() == null || p.getParentId() == 0)
                .collect(Collectors.toList());

        return roots;
    }
}
