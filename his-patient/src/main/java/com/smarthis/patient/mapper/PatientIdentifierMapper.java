package com.smarthis.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.patient.entity.PatientIdentifier;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientIdentifierMapper extends BaseMapper<PatientIdentifier> {
}
