package com.smarthis.patient.service;

import com.smarthis.patient.entity.ScheduleTemplate;

import java.util.List;

public interface ScheduleTemplateService {
    List<ScheduleTemplate> listByDoctor(Long doctorId);
    List<ScheduleTemplate> listByDept(Long deptId);
}
