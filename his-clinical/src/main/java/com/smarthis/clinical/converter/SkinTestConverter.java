package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.SkinTestCreateRequest;
import com.smarthis.clinical.dto.response.SkinTestVo;
import com.smarthis.clinical.entity.SkinTest;

public final class SkinTestConverter {

    private SkinTestConverter() {
    }

    public static SkinTest toEntity(SkinTestCreateRequest req) {
        SkinTest e = new SkinTest();
        e.setPatientId(req.getPatientId());
        e.setAdmissionId(req.getAdmissionId());
        e.setOrderItemId(req.getOrderItemId());
        e.setDrugCode(req.getDrugCode());
        e.setDrugName(req.getDrugName());
        e.setBatchNo(req.getBatchNo());
        e.setTestTime(req.getTestTime());
        e.setTestNurseId(req.getTestNurseId());
        e.setRemark(req.getRemark());
        return e;
    }

    public static SkinTestVo toVo(SkinTest e) {
        SkinTestVo vo = new SkinTestVo();
        vo.setId(e.getId());
        vo.setPatientId(e.getPatientId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setOrderItemId(e.getOrderItemId());
        vo.setDrugCode(e.getDrugCode());
        vo.setDrugName(e.getDrugName());
        vo.setBatchNo(e.getBatchNo());
        vo.setTestResult(e.getTestResult());
        vo.setTestTime(e.getTestTime());
        vo.setTestNurseId(e.getTestNurseId());
        vo.setJudgeTime(e.getJudgeTime());
        vo.setJudgeNurseId(e.getJudgeNurseId());
        vo.setRemark(e.getRemark());
        return vo;
    }
}
