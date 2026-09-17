package com.smarthis.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.auth.entity.AuthUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<AuthUser> {

    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Update("UPDATE auth_user SET mfa_enabled = 0, mfa_secret = NULL, updated_time = CURRENT_TIMESTAMP WHERE id = #{userId}")
    int clearMfa(@Param("userId") Long userId);
}
