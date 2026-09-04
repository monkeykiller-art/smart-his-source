package com.smarthis.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.auth.entity.AuthLoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper extends BaseMapper<AuthLoginLog> {
}
