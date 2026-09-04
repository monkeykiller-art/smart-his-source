package com.smarthis.patient.converter;

import com.smarthis.patient.dto.request.PatientCreateRequest;
import com.smarthis.patient.dto.response.PatientVo;
import com.smarthis.patient.entity.Patient;

public final class PatientConverter {

    private PatientConverter() {
    }

    public static Patient toEntity(PatientCreateRequest req) {
        Patient p = new Patient();
        p.setName(req.getName());
        p.setGender(req.getGender());
        p.setBirthDate(req.getBirthDate());
        p.setIdType(req.getIdType());
        p.setIdNo(req.getIdNo());
        p.setNationality(req.getNationality());
        p.setNation(req.getNation());
        p.setMaritalStatus(req.getMaritalStatus());
        p.setOccupation(req.getOccupation());
        p.setPhone(req.getPhone());
        p.setPhoneBackup(req.getPhoneBackup());
        p.setAddress(req.getAddress());
        p.setBloodType(req.getBloodType());
        p.setAllergyHistory(req.getAllergyHistory());
        p.setInsuranceType(req.getInsuranceType());
        p.setInsuranceNo(req.getInsuranceNo());
        return p;
    }

    public static PatientVo toVo(Patient p) {
        PatientVo vo = new PatientVo();
        vo.setId(p.getId());
        vo.setEmpiNo(p.getEmpiNo());
        vo.setName(p.getName());
        vo.setNamePinyin(p.getNamePinyin());
        vo.setGender(p.getGender());
        vo.setBirthDate(p.getBirthDate());
        vo.setAgeDisplay(p.getAgeDisplay());
        vo.setIdType(p.getIdType());
        vo.setIdNo(p.getIdNo());
        vo.setNationality(p.getNationality());
        vo.setNation(p.getNation());
        vo.setMaritalStatus(p.getMaritalStatus());
        vo.setOccupation(p.getOccupation());
        vo.setPhone(p.getPhone());
        vo.setPhoneBackup(p.getPhoneBackup());
        vo.setAddress(p.getAddress());
        vo.setBloodType(p.getBloodType());
        vo.setAllergyHistory(p.getAllergyHistory());
        vo.setInsuranceType(p.getInsuranceType());
        vo.setInsuranceNo(p.getInsuranceNo());
        vo.setPatientType(p.getPatientType());
        vo.setPatientStatus(p.getPatientStatus());
        return vo;
    }
}
