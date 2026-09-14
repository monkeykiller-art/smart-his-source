package com.smarthis.operations.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.operations.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    @Select("SELECT * FROM ops_bill WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Bill selectByIdForUpdate(@Param("id") Long id);
}
