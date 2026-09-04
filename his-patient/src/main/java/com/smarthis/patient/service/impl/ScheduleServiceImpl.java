package com.smarthis.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.patient.converter.ScheduleConverter;
import com.smarthis.patient.dto.request.ScheduleGenerateRequest;
import com.smarthis.patient.dto.request.ScheduleQueryRequest;
import com.smarthis.patient.dto.response.ScheduleVo;
import com.smarthis.patient.entity.Department;
import com.smarthis.patient.entity.Doctor;
import com.smarthis.patient.entity.Schedule;
import com.smarthis.patient.entity.ScheduleTemplate;
import com.smarthis.patient.mapper.DepartmentMapper;
import com.smarthis.patient.mapper.DoctorMapper;
import com.smarthis.patient.mapper.ScheduleMapper;
import com.smarthis.patient.service.ScheduleService;
import com.smarthis.patient.service.ScheduleTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final ScheduleTemplateService templateService;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public int generate(ScheduleGenerateRequest request) {
        List<ScheduleTemplate> templates;
        if (request.getDoctorId() != null) {
            templates = templateService.listByDoctor(request.getDoctorId());
        } else if (request.getDeptId() != null) {
            templates = templateService.listByDept(request.getDeptId());
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "doctorId or deptId is required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "endDate must not be before startDate");
        }

        int count = 0;
        LocalDate date = request.getStartDate();
        while (!date.isAfter(request.getEndDate())) {
            int dow = date.getDayOfWeek().getValue();
            for (ScheduleTemplate tpl : templates) {
                if (tpl.getDayOfWeek() == null || tpl.getDayOfWeek() != dow) {
                    continue;
                }
                LambdaQueryWrapper<Schedule> existQuery = new LambdaQueryWrapper<>();
                existQuery.eq(Schedule::getDoctorId, tpl.getDoctorId())
                        .eq(Schedule::getScheduleDate, date)
                        .eq(Schedule::getTimePeriod, tpl.getTimePeriod())
                        .eq(Schedule::getDeleted, 0);
                if (scheduleMapper.selectCount(existQuery) > 0) {
                    continue;
                }
                Schedule schedule = new Schedule();
                schedule.setDeptId(tpl.getDeptId());
                schedule.setDoctorId(tpl.getDoctorId());
                schedule.setScheduleDate(date);
                schedule.setTimePeriod(tpl.getTimePeriod());
                schedule.setStartTime(tpl.getStartTime());
                schedule.setEndTime(tpl.getEndTime());
                schedule.setTotalQuota(tpl.getTotalQuota());
                schedule.setUsedQuota(0);
                schedule.setRegFee(tpl.getRegFee());
                schedule.setRegLevel(tpl.getRegLevel());
                schedule.setScheduleStatus("ACTIVE");
                schedule.setRevision(0);
                scheduleMapper.insert(schedule);
                count++;
            }
            date = date.plusDays(1);
        }
        log.info("Generated {} schedules from {} to {}", count, request.getStartDate(), request.getEndDate());
        return count;
    }

    @Override
    public PageResult<ScheduleVo> query(ScheduleQueryRequest request) {
        LambdaQueryWrapper<Schedule> query = new LambdaQueryWrapper<>();
        query.eq(Schedule::getDeleted, 0);
        if (request.getDeptId() != null) {
            query.eq(Schedule::getDeptId, request.getDeptId());
        }
        if (request.getDoctorId() != null) {
            query.eq(Schedule::getDoctorId, request.getDoctorId());
        }
        if (request.getScheduleDate() != null) {
            query.eq(Schedule::getScheduleDate, request.getScheduleDate());
        }
        if (StringUtils.hasText(request.getTimePeriod())) {
            query.eq(Schedule::getTimePeriod, request.getTimePeriod());
        }
        query.orderByAsc(Schedule::getScheduleDate).orderByAsc(Schedule::getTimePeriod);

        Page<Schedule> page = request.toPage();
        IPage<Schedule> result = scheduleMapper.selectPage(page, query);
        List<ScheduleVo> vos = result.getRecords().stream()
                .map(this::enrichVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public ScheduleVo getById(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null || schedule.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        return enrichVo(schedule);
    }

    @Override
    @Transactional
    public void stop(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null || schedule.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        schedule.setScheduleStatus("STOPPED");
        scheduleMapper.updateById(schedule);
        log.info("Schedule stopped: id={}", id);
    }

    private ScheduleVo enrichVo(Schedule s) {
        ScheduleVo vo = ScheduleConverter.toVo(s);
        if (s.getDoctorId() != null) {
            Doctor doc = doctorMapper.selectById(s.getDoctorId());
            if (doc != null) {
                vo.setDoctorName(doc.getDoctorName());
            }
        }
        if (s.getDeptId() != null) {
            Department dept = departmentMapper.selectById(s.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        return vo;
    }
}
