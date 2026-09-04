package com.smarthis.operations.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.operations.entity.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
