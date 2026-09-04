package com.smarthis.auth.service;

import com.smarthis.auth.entity.AuthPermission;
import com.smarthis.common.model.PageQuery;
import com.smarthis.common.model.PageResult;

import java.util.List;

public interface PermissionService {

    AuthPermission createPermission(AuthPermission permission);

    AuthPermission getPermissionById(Long id);

    PageResult<AuthPermission> listPermissions(String moduleCode, PageQuery pageQuery);

    List<AuthPermission> listByModule(String moduleCode);

    List<AuthPermission> listTree();
}
