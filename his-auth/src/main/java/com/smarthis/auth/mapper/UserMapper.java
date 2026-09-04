package com.smarthis.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.auth.entity.AuthUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<AuthUser> {

    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
