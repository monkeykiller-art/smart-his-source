package com.smarthis.patient.converter;

import com.smarthis.patient.dto.response.RegistrationVo;
import com.smarthis.patient.entity.Registration;

public final class RegistrationConverter {

    private RegistrationConverter() {
    }

    public static RegistrationVo toVo(Registration r) {
        RegistrationVo vo = new RegistrationVo();
        vo.setId(r.getId());
        vo.setRegNo(r.getRegNo());
        vo.setPatientId(r.getPatientId());
        vo.setScheduleId(r.getScheduleId());
        vo.setDeptId(r.getDeptId());
        vo.setDoctorId(r.getDoctorId());
        vo.setVisitSeq(r.getVisitSeq());
        vo.setRegDate(r.getRegDate());
        vo.setTimePeriod(r.getTimePeriod());
        vo.setRegFee(r.getRegFee());
        vo.setPayStatus(r.getPayStatus());
        vo.setPayTime(r.getPayTime());
        vo.setRegSource(r.getRegSource());
        vo.setRegStatus(r.getRegStatus());
        vo.setBillId(r.getBillId());
        vo.setCreatedTime(r.getCreatedTime());
        return vo;
    }
}
