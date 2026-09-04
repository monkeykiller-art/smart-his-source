package com.smarthis.auth.service;

import com.smarthis.auth.entity.AuthRole;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;

import java.util.List;

public interface RoleService {

    AuthRole createRole(AuthRole role);

    AuthRole getRoleById(Long id);

    PageResult<AuthRole> listRoles(String keyword, PageQuery pageQuery);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    List<String> getPermissionCodesByUserId(Long userId);
}
