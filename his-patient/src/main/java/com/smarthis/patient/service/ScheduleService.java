package com.smarthis.patient.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.patient.dto.request.ScheduleGenerateRequest;
import com.smarthis.patient.dto.request.ScheduleQueryRequest;
import com.smarthis.patient.dto.response.ScheduleVo;

public interface ScheduleService {
    int generate(ScheduleGenerateRequest request);
    PageResult<ScheduleVo> query(ScheduleQueryRequest request);
    ScheduleVo getById(Long id);
    void stop(Long id);
}
