package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.DiagnosisCreateRequest;
import com.smarthis.clinical.dto.response.DiagnosisVo;
import com.smarthis.clinical.entity.Diagnosis;

public final class DiagnosisConverter {

    private DiagnosisConverter() {
    }

    public static Diagnosis toEntity(DiagnosisCreateRequest req) {
        Diagnosis e = new Diagnosis();
        e.setPatientId(req.getPatientId());
        e.setEncounterId(req.getEncounterId());
        e.setAdmissionId(req.getAdmissionId());
        e.setDoctorId(req.getDoctorId());
        e.setIcd10Id(req.getIcd10Id());
        e.setIcdCode(req.getIcdCode());
        e.setDiagnosisName(req.getDiagnosisName());
        e.setDiagnosisType(req.getDiagnosisType() != null ? req.getDiagnosisType() : "WESTERN");
        e.setIsPrimary(req.getIsPrimary() != null ? req.getIsPrimary() : 0);
        e.setIsConfirmed(req.getIsConfirmed() != null ? req.getIsConfirmed() : 0);
        e.setDiagnosisSeq(req.getDiagnosisSeq() != null ? req.getDiagnosisSeq() : 1);
        e.setOnsetDate(req.getOnsetDate());
        e.setDiagnosisDesc(req.getDiagnosisDesc());
        e.setDiagnosisStatus("ACTIVE");
        return e;
    }

    public static DiagnosisVo toVo(Diagnosis e) {
        DiagnosisVo vo = new DiagnosisVo();
        vo.setId(e.getId());
        vo.setEncounterId(e.getEncounterId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setPatientId(e.getPatientId());
        vo.setDoctorId(e.getDoctorId());
        vo.setIcd10Id(e.getIcd10Id());
        vo.setIcdCode(e.getIcdCode());
        vo.setDiagnosisName(e.getDiagnosisName());
        vo.setDiagnosisType(e.getDiagnosisType());
        vo.setIsPrimary(e.getIsPrimary());
        vo.setIsConfirmed(e.getIsConfirmed());
        vo.setDiagnosisSeq(e.getDiagnosisSeq());
        vo.setOnsetDate(e.getOnsetDate());
        vo.setDiagnosisDesc(e.getDiagnosisDesc());
        vo.setDiagnosisStatus(e.getDiagnosisStatus());
        return vo;
    }
}
