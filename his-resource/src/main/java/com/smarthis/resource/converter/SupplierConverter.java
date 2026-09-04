package com.smarthis.resource.converter;

import com.smarthis.resource.dto.request.SupplierCreateRequest;
import com.smarthis.resource.dto.request.SupplierUpdateRequest;
import com.smarthis.resource.dto.response.SupplierVo;
import com.smarthis.resource.entity.Supplier;

public final class SupplierConverter {

    private SupplierConverter() {
    }

    public static Supplier toEntity(SupplierCreateRequest req) {
        Supplier e = new Supplier();
        e.setSupplierCode(req.getSupplierCode());
        e.setSupplierName(req.getSupplierName());
        e.setContactPerson(req.getContactPerson());
        e.setContactPhone(req.getContactPhone());
        e.setAddress(req.getAddress());
        e.setLicenseNo(req.getLicenseNo());
        e.setSupplierType(req.getSupplierType() != null ? req.getSupplierType() : "NORMAL");
        e.setSupplierStatus("ACTIVE");
        return e;
    }

    public static void applyUpdate(SupplierUpdateRequest req, Supplier e) {
        if (req.getSupplierName() != null) e.setSupplierName(req.getSupplierName());
        if (req.getContactPerson() != null) e.setContactPerson(req.getContactPerson());
        if (req.getContactPhone() != null) e.setContactPhone(req.getContactPhone());
        if (req.getAddress() != null) e.setAddress(req.getAddress());
        if (req.getLicenseNo() != null) e.setLicenseNo(req.getLicenseNo());
        if (req.getSupplierType() != null) e.setSupplierType(req.getSupplierType());
        if (req.getSupplierStatus() != null) e.setSupplierStatus(req.getSupplierStatus());
    }

    public static SupplierVo toVo(Supplier e) {
        SupplierVo vo = new SupplierVo();
        vo.setId(e.getId());
        vo.setSupplierCode(e.getSupplierCode());
        vo.setSupplierName(e.getSupplierName());
        vo.setContactPerson(e.getContactPerson());
        vo.setContactPhone(e.getContactPhone());
        vo.setAddress(e.getAddress());
        vo.setLicenseNo(e.getLicenseNo());
        vo.setSupplierType(e.getSupplierType());
        vo.setSupplierStatus(e.getSupplierStatus());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }
}
