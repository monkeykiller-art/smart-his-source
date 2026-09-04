package com.smarthis.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.auth.entity.AuthPermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<AuthPermission> {
}
