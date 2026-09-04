package com.smarthis.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.auth.entity.AuthUserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper extends BaseMapper<AuthUserRole> {
}
