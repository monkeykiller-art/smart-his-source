package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.patient.entity.ScheduleTemplate;
import com.smarthis.patient.mapper.ScheduleTemplateMapper;
import com.smarthis.patient.service.ScheduleTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleTemplateServiceImpl implements ScheduleTemplateService {

    private final ScheduleTemplateMapper templateMapper;

    @Override
    public List<ScheduleTemplate> listByDoctor(Long doctorId) {
        LambdaQueryWrapper<ScheduleTemplate> query = new LambdaQueryWrapper<>();
        query.eq(ScheduleTemplate::getDeleted, 0)
                .eq(ScheduleTemplate::getDoctorId, doctorId)
                .eq(ScheduleTemplate::getTemplateStatus, "ACTIVE")
                .orderByAsc(ScheduleTemplate::getDayOfWeek);
        return templateMapper.selectList(query);
    }

    @Override
    public List<ScheduleTemplate> listByDept(Long deptId) {
        LambdaQueryWrapper<ScheduleTemplate> query = new LambdaQueryWrapper<>();
        query.eq(ScheduleTemplate::getDeleted, 0)
                .eq(ScheduleTemplate::getDeptId, deptId)
                .eq(ScheduleTemplate::getTemplateStatus, "ACTIVE")
                .orderByAsc(ScheduleTemplate::getDayOfWeek);
        return templateMapper.selectList(query);
    }
}
