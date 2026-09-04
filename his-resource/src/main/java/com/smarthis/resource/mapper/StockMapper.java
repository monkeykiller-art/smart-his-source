package com.smarthis.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.resource.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}
