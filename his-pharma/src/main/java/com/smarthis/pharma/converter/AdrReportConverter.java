package com.smarthis.pharma.converter;

import com.smarthis.pharma.dto.request.AdrReportCreateRequest;
import com.smarthis.pharma.dto.response.AdrReportVo;
import com.smarthis.pharma.entity.AdrReport;

public final class AdrReportConverter {

    private AdrReportConverter() {
    }

    public static AdrReport toEntity(AdrReportCreateRequest req) {
        AdrReport e = new AdrReport();
        e.setPatientId(req.getPatientId());
        e.setAdmissionId(req.getAdmissionId());
        e.setDrugCode(req.getDrugCode());
        e.setDrugName(req.getDrugName());
        e.setBatchNo(req.getBatchNo());
        e.setAdrOnsetTime(req.getAdrOnsetTime());
        e.setAdrType(req.getAdrType());
        e.setAdrLevel(req.getAdrLevel());
        e.setAdrDesc(req.getAdrDesc());
        e.setAdrOutcome(req.getAdrOutcome());
        e.setReporterId(req.getReporterId());
        e.setReporterName(req.getReporterName());
        e.setReportDeptId(req.getReportDeptId());
        e.setReportStatus("SUBMITTED");
        e.setIsReportedToAuthority(0);
        e.setRemark(req.getRemark());
        return e;
    }

    public static AdrReportVo toVo(AdrReport e) {
        AdrReportVo vo = new AdrReportVo();
        vo.setId(e.getId());
        vo.setReportNo(e.getReportNo());
        vo.setPatientId(e.getPatientId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setDrugCode(e.getDrugCode());
        vo.setDrugName(e.getDrugName());
        vo.setBatchNo(e.getBatchNo());
        vo.setAdrOnsetTime(e.getAdrOnsetTime());
        vo.setAdrType(e.getAdrType());
        vo.setAdrLevel(e.getAdrLevel());
        vo.setAdrDesc(e.getAdrDesc());
        vo.setAdrOutcome(e.getAdrOutcome());
        vo.setReporterId(e.getReporterId());
        vo.setReporterName(e.getReporterName());
        vo.setReportDeptId(e.getReportDeptId());
        vo.setReportTime(e.getReportTime());
        vo.setReportStatus(e.getReportStatus());
        vo.setReviewComment(e.getReviewComment());
        vo.setReviewerId(e.getReviewerId());
        vo.setReviewTime(e.getReviewTime());
        vo.setIsReportedToAuthority(e.getIsReportedToAuthority());
        vo.setRemark(e.getRemark());
        return vo;
    }
}
