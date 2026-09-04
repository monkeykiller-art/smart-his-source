package com.smarthis.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.patient.entity.Admission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdmissionMapper extends BaseMapper<Admission> {
}
