package com.smarthis.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.auth.entity.AuthPermission;
import com.smarthis.auth.entity.AuthRole;
import com.smarthis.auth.entity.AuthRolePermission;
import com.smarthis.auth.mapper.PermissionMapper;
import com.smarthis.auth.mapper.RoleMapper;
import com.smarthis.auth.mapper.RolePermissionMapper;
import com.smarthis.auth.mapper.UserMapper;
import com.smarthis.auth.service.RoleService;
import com.smarthis.common.context.UserContextHolder;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthRole createRole(AuthRole role) {
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<AuthRole>().eq(AuthRole::getRoleCode, role.getRoleCode()));
        if (count > 0) {
            throw new BusinessException(400, "role code already exists");
        }
        role.setRoleStatus("ACTIVE");
        if (role.getBuiltin() == null) {
            role.setBuiltin(0);
        }
        if (role.getDataScope() == null) {
            role.setDataScope("ALL");
        }
        if (role.getSortOrder() == null) {
            role.setSortOrder(0);
        }
        roleMapper.insert(role);
        return role;
    }

    @Override
    public AuthRole getRoleById(Long id) {
        AuthRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return role;
    }

    @Override
    public PageResult<AuthRole> listRoles(String keyword, PageQuery pageQuery) {
        LambdaQueryWrapper<AuthRole> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(AuthRole::getRoleCode, keyword)
                    .or().like(AuthRole::getRoleName, keyword));
        }
        wrapper.orderByAsc(AuthRole::getSortOrder);

        Page<AuthRole> page = roleMapper.selectPage(pageQuery.toPage(), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(),
                pageQuery.getPage(), pageQuery.getSize());
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<AuthRolePermission>()
                        .eq(AuthRolePermission::getRoleId, roleId));
        String operator = UserContextHolder.getUsername();
        for (Long permId : permissionIds) {
            AuthRolePermission rp = new AuthRolePermission();
            rp.setId(Math.abs(UUID.randomUUID().getMostSignificantBits()));
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rp.setCreatedBy(operator);
            rp.setCreatedTime(LocalDateTime.now());
            rolePermissionMapper.insert(rp);
        }
    }

    @Override
    public List<String> getPermissionCodesByUserId(Long userId) {
        return userMapper.selectPermCodesByUserId(userId);
    }
}
