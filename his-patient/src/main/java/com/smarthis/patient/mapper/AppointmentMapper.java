package com.smarthis.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthis.patient.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
