package com.smarthis.resource.converter;

import com.smarthis.resource.dto.request.PharmacyCreateRequest;
import com.smarthis.resource.dto.request.PharmacyUpdateRequest;
import com.smarthis.resource.dto.response.PharmacyVo;
import com.smarthis.resource.entity.Pharmacy;

public final class PharmacyConverter {

    private PharmacyConverter() {
    }

    public static Pharmacy toEntity(PharmacyCreateRequest req) {
        Pharmacy e = new Pharmacy();
        e.setPharmacyCode(req.getPharmacyCode());
        e.setPharmacyName(req.getPharmacyName());
        e.setPharmacyType(req.getPharmacyType() != null ? req.getPharmacyType() : "OUTPATIENT");
        e.setDeptId(req.getDeptId());
        e.setLocation(req.getLocation());
        e.setPhone(req.getPhone());
        e.setPharmacyStatus("ACTIVE");
        return e;
    }

    public static void applyUpdate(PharmacyUpdateRequest req, Pharmacy e) {
        if (req.getPharmacyName() != null) e.setPharmacyName(req.getPharmacyName());
        if (req.getPharmacyType() != null) e.setPharmacyType(req.getPharmacyType());
        if (req.getLocation() != null) e.setLocation(req.getLocation());
        if (req.getPhone() != null) e.setPhone(req.getPhone());
        if (req.getPharmacyStatus() != null) e.setPharmacyStatus(req.getPharmacyStatus());
    }

    public static PharmacyVo toVo(Pharmacy e) {
        PharmacyVo vo = new PharmacyVo();
        vo.setId(e.getId());
        vo.setPharmacyCode(e.getPharmacyCode());
        vo.setPharmacyName(e.getPharmacyName());
        vo.setPharmacyType(e.getPharmacyType());
        vo.setDeptId(e.getDeptId());
        vo.setLocation(e.getLocation());
        vo.setPhone(e.getPhone());
        vo.setPharmacyStatus(e.getPharmacyStatus());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }
}
