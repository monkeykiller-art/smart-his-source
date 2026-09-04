package com.smarthis.patient.converter;

import com.smarthis.patient.dto.request.AdmissionCreateRequest;
import com.smarthis.patient.dto.response.AdmissionVo;
import com.smarthis.patient.entity.Admission;

public final class AdmissionConverter {

    private AdmissionConverter() {}

    public static Admission toEntity(AdmissionCreateRequest request) {
        Admission admission = new Admission();
        admission.setPatientId(request.getPatientId());
        admission.setEncounterId(request.getEncounterId());
        admission.setAdmissionType(request.getAdmissionType() != null ? request.getAdmissionType() : "ELECTIVE");
        admission.setAdmissionStatus("PLANNED");
        admission.setDeptId(request.getDeptId());
        admission.setDoctorId(request.getDoctorId());
        admission.setWardId(request.getWardId());
        admission.setBedId(request.getBedId());
        admission.setInsuranceType(request.getInsuranceType());
        admission.setInsuranceNo(request.getInsuranceNo());
        admission.setInsuranceOrg(request.getInsuranceOrg());
        admission.setEmergencyContactName(request.getEmergencyContactName());
        admission.setEmergencyContactPhone(request.getEmergencyContactPhone());
        admission.setEmergencyContactAddr(request.getEmergencyContactAddr());
        admission.setAdmissionDate(request.getAdmissionDate());
        admission.setExpectedDischargeDate(request.getExpectedDischargeDate());
        admission.setChiefComplaint(request.getChiefComplaint());
        admission.setPreliminaryDiagnosis(request.getPreliminaryDiagnosis());
        admission.setDepositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : java.math.BigDecimal.ZERO);
        admission.setTotalDeposit(request.getDepositAmount() != null ? request.getDepositAmount() : java.math.BigDecimal.ZERO);
        return admission;
    }

    public static AdmissionVo toVo(Admission admission) {
        AdmissionVo vo = new AdmissionVo();
        vo.setId(admission.getId());
        vo.setAdmissionNo(admission.getAdmissionNo());
        vo.setPatientId(admission.getPatientId());
        vo.setEncounterId(admission.getEncounterId());
        vo.setAdmissionType(admission.getAdmissionType());
        vo.setAdmissionStatus(admission.getAdmissionStatus());
        vo.setDeptId(admission.getDeptId());
        vo.setDoctorId(admission.getDoctorId());
        vo.setWardId(admission.getWardId());
        vo.setBedId(admission.getBedId());
        vo.setInsuranceType(admission.getInsuranceType());
        vo.setInsuranceNo(admission.getInsuranceNo());
        vo.setInsuranceOrg(admission.getInsuranceOrg());
        vo.setEmergencyContactName(admission.getEmergencyContactName());
        vo.setEmergencyContactPhone(admission.getEmergencyContactPhone());
        vo.setEmergencyContactAddr(admission.getEmergencyContactAddr());
        vo.setAdmissionDate(admission.getAdmissionDate());
        vo.setExpectedDischargeDate(admission.getExpectedDischargeDate());
        vo.setActualDischargeDate(admission.getActualDischargeDate());
        vo.setChiefComplaint(admission.getChiefComplaint());
        vo.setPreliminaryDiagnosis(admission.getPreliminaryDiagnosis());
        vo.setDepositAmount(admission.getDepositAmount());
        vo.setTotalDeposit(admission.getTotalDeposit());
        vo.setDischargeType(admission.getDischargeType());
        vo.setDischargeSummary(admission.getDischargeSummary());
        vo.setCreatedTime(admission.getCreatedTime());
        vo.setUpdatedTime(admission.getUpdatedTime());
        return vo;
    }
}
