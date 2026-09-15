package com.smarthis.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.patient.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
    @Select("SELECT * FROM pat_registration WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Registration selectByIdForUpdate(@Param("id") Long id);

    @Select("SELECT id FROM pat_registration WHERE deleted = 0 AND reg_status = 'ACTIVE' ORDER BY COALESCE(updated_time, created_time), id LIMIT 50")
    List<Long> billingSyncIds();

    @Update("UPDATE pat_registration SET updated_time = CURRENT_TIMESTAMP WHERE id = #{id} AND deleted = 0")
    void touchBillingAttempt(@Param("id") Long id);
}
