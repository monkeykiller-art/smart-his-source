package com.smarthis.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.auth.entity.AuthRolePermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RolePermissionMapper extends BaseMapper<AuthRolePermission> {
}
