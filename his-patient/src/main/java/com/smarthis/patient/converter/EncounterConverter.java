package com.smarthis.patient.converter;

import com.smarthis.patient.dto.response.EncounterVo;
import com.smarthis.patient.entity.Encounter;

public final class EncounterConverter {

    private EncounterConverter() {
    }

    public static EncounterVo toVo(Encounter e) {
        EncounterVo vo = new EncounterVo();
        vo.setId(e.getId());
        vo.setEncounterNo(e.getEncounterNo());
        vo.setPatientId(e.getPatientId());
        vo.setRegId(e.getRegId());
        vo.setDeptId(e.getDeptId());
        vo.setDoctorId(e.getDoctorId());
        vo.setEncounterType(e.getEncounterType());
        vo.setEncounterStatus(e.getEncounterStatus());
        vo.setVisitDate(e.getVisitDate());
        vo.setStartTime(e.getStartTime());
        vo.setEndTime(e.getEndTime());
        vo.setChiefComplaint(e.getChiefComplaint());
        return vo;
    }
}
