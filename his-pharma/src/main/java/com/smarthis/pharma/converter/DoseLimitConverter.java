package com.smarthis.pharma.converter;

import com.smarthis.pharma.dto.request.DoseLimitCreateRequest;
import com.smarthis.pharma.dto.response.DoseLimitVo;
import com.smarthis.pharma.entity.DoseLimit;

public final class DoseLimitConverter {

    private DoseLimitConverter() {
    }

    public static DoseLimit toEntity(DoseLimitCreateRequest req) {
        DoseLimit e = new DoseLimit();
        e.setDrugCode(req.getDrugCode());
        e.setPatientType(req.getPatientType());
        e.setAgeMin(req.getAgeMin());
        e.setAgeMax(req.getAgeMax());
        e.setRoute(req.getRoute());
        e.setMaxSingleDose(req.getMaxSingleDose());
        e.setMaxSingleUnit(req.getMaxSingleUnit());
        e.setMaxDailyDose(req.getMaxDailyDose());
        e.setMaxDailyUnit(req.getMaxDailyUnit());
        e.setMaxFreqPerDay(req.getMaxFreqPerDay());
        e.setDescription(req.getDescription());
        e.setIsActive(1);
        return e;
    }

    public static DoseLimitVo toVo(DoseLimit e) {
        DoseLimitVo vo = new DoseLimitVo();
        vo.setId(e.getId());
        vo.setDrugCode(e.getDrugCode());
        vo.setPatientType(e.getPatientType());
        vo.setAgeMin(e.getAgeMin());
        vo.setAgeMax(e.getAgeMax());
        vo.setRoute(e.getRoute());
        vo.setMaxSingleDose(e.getMaxSingleDose());
        vo.setMaxSingleUnit(e.getMaxSingleUnit());
        vo.setMaxDailyDose(e.getMaxDailyDose());
        vo.setMaxDailyUnit(e.getMaxDailyUnit());
        vo.setMaxFreqPerDay(e.getMaxFreqPerDay());
        vo.setDescription(e.getDescription());
        vo.setIsActive(e.getIsActive());
        return vo;
    }
}
