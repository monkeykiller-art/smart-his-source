package com.smarthis.clinical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.clinical.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
