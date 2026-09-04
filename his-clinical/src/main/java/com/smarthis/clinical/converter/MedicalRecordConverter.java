package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.MedicalRecordCreateRequest;
import com.smarthis.clinical.dto.request.MedicalRecordUpdateRequest;
import com.smarthis.clinical.dto.response.MedicalRecordVo;
import com.smarthis.clinical.entity.MedicalRecord;

public final class MedicalRecordConverter {

    private MedicalRecordConverter() {
    }

    public static MedicalRecord toEntity(MedicalRecordCreateRequest req) {
        MedicalRecord e = new MedicalRecord();
        e.setPatientId(req.getPatientId());
        e.setEncounterId(req.getEncounterId());
        e.setAdmissionId(req.getAdmissionId());
        e.setDeptId(req.getDeptId());
        e.setDoctorId(req.getDoctorId());
        e.setRecordType(req.getRecordType());
        e.setTemplateId(req.getTemplateId());
        e.setTitle(req.getTitle());
        e.setChiefComplaint(req.getChiefComplaint());
        e.setPresentIllness(req.getPresentIllness());
        e.setPastHistory(req.getPastHistory());
        e.setAllergyHistory(req.getAllergyHistory());
        e.setPhysicalExam(req.getPhysicalExam());
        e.setAuxiliaryExam(req.getAuxiliaryExam());
        e.setDiagnosisDesc(req.getDiagnosisDesc());
        e.setTreatmentPlan(req.getTreatmentPlan());
        e.setRecordContent(req.getRecordContent());
        e.setRecordStatus("DRAFT");
        return e;
    }

    public static void applyUpdate(MedicalRecord e, MedicalRecordUpdateRequest req) {
        if (req.getTitle() != null) e.setTitle(req.getTitle());
        if (req.getChiefComplaint() != null) e.setChiefComplaint(req.getChiefComplaint());
        if (req.getPresentIllness() != null) e.setPresentIllness(req.getPresentIllness());
        if (req.getPastHistory() != null) e.setPastHistory(req.getPastHistory());
        if (req.getAllergyHistory() != null) e.setAllergyHistory(req.getAllergyHistory());
        if (req.getPhysicalExam() != null) e.setPhysicalExam(req.getPhysicalExam());
        if (req.getAuxiliaryExam() != null) e.setAuxiliaryExam(req.getAuxiliaryExam());
        if (req.getDiagnosisDesc() != null) e.setDiagnosisDesc(req.getDiagnosisDesc());
        if (req.getTreatmentPlan() != null) e.setTreatmentPlan(req.getTreatmentPlan());
        if (req.getRecordContent() != null) e.setRecordContent(req.getRecordContent());
    }

    public static MedicalRecordVo toVo(MedicalRecord e) {
        MedicalRecordVo vo = new MedicalRecordVo();
        vo.setId(e.getId());
        vo.setRecordNo(e.getRecordNo());
        vo.setEncounterId(e.getEncounterId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setPatientId(e.getPatientId());
        vo.setDeptId(e.getDeptId());
        vo.setDoctorId(e.getDoctorId());
        vo.setRecordType(e.getRecordType());
        vo.setTemplateId(e.getTemplateId());
        vo.setTitle(e.getTitle());
        vo.setChiefComplaint(e.getChiefComplaint());
        vo.setPresentIllness(e.getPresentIllness());
        vo.setPastHistory(e.getPastHistory());
        vo.setAllergyHistory(e.getAllergyHistory());
        vo.setPhysicalExam(e.getPhysicalExam());
        vo.setAuxiliaryExam(e.getAuxiliaryExam());
        vo.setDiagnosisDesc(e.getDiagnosisDesc());
        vo.setTreatmentPlan(e.getTreatmentPlan());
        vo.setRecordContent(e.getRecordContent());
        vo.setRecordStatus(e.getRecordStatus());
        vo.setSignTime(e.getSignTime());
        vo.setQualityScore(e.getQualityScore());
        vo.setQualityResult(e.getQualityResult());
        vo.setCreatedTime(e.getCreatedTime());
        vo.setUpdatedTime(e.getUpdatedTime());
        return vo;
    }
}
