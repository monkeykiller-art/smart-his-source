package com.smarthis.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.patient.entity.Encounter;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EncounterMapper extends BaseMapper<Encounter> {
}
