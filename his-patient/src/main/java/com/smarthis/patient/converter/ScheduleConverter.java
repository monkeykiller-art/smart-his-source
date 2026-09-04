package com.smarthis.patient.converter;

import com.smarthis.patient.dto.response.ScheduleVo;
import com.smarthis.patient.entity.Schedule;

public final class ScheduleConverter {

    private ScheduleConverter() {
    }

    public static ScheduleVo toVo(Schedule s) {
        ScheduleVo vo = new ScheduleVo();
        vo.setId(s.getId());
        vo.setDeptId(s.getDeptId());
        vo.setDoctorId(s.getDoctorId());
        vo.setScheduleDate(s.getScheduleDate());
        vo.setTimePeriod(s.getTimePeriod());
        vo.setStartTime(s.getStartTime());
        vo.setEndTime(s.getEndTime());
        vo.setTotalQuota(s.getTotalQuota());
        vo.setUsedQuota(s.getUsedQuota());
        vo.setAvailableQuota(s.getTotalQuota() - s.getUsedQuota());
        vo.setRegFee(s.getRegFee());
        vo.setRegLevel(s.getRegLevel());
        vo.setScheduleStatus(s.getScheduleStatus());
        return vo;
    }
}
