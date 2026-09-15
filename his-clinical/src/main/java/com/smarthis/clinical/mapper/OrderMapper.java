package com.smarthis.clinical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.clinical.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Select("SELECT * FROM cli_order WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Order selectByIdForUpdate(@Param("id") Long id);

    @Select("SELECT id FROM cli_order WHERE deleted = 0 AND bill_id IS NULL "
            + "AND order_status IN ('SUBMITTED', 'VERIFIED', 'EXECUTING', 'COMPLETED') "
            + "ORDER BY COALESCE(updated_time, order_time), id LIMIT 20")
    List<Long> pendingBillIds();
}
