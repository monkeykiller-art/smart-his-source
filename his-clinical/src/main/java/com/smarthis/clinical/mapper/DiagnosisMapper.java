package com.smarthis.clinical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.clinical.entity.Diagnosis;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DiagnosisMapper extends BaseMapper<Diagnosis> {
}
