package com.smarthis.operations.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.operations.entity.Bill;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {
}
