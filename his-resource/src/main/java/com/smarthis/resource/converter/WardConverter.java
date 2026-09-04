package com.smarthis.resource.converter;

import com.smarthis.resource.dto.request.WardCreateRequest;
import com.smarthis.resource.dto.request.WardUpdateRequest;
import com.smarthis.resource.dto.response.WardVo;
import com.smarthis.resource.entity.Ward;

public final class WardConverter {

    private WardConverter() {
    }

    public static Ward toEntity(WardCreateRequest req) {
        Ward e = new Ward();
        e.setWardCode(req.getWardCode());
        e.setWardName(req.getWardName());
        e.setDeptId(req.getDeptId());
        e.setWardType(req.getWardType() != null ? req.getWardType() : "GENERAL");
        e.setFloorLocation(req.getFloorLocation());
        e.setBedCount(req.getBedCount());
        e.setNurseStation(req.getNurseStation());
        e.setHeadNurseId(req.getHeadNurseId());
        e.setWardStatus("ACTIVE");
        e.setDescription(req.getDescription());
        return e;
    }

    public static void applyUpdate(WardUpdateRequest req, Ward e) {
        if (req.getWardName() != null) e.setWardName(req.getWardName());
        if (req.getWardType() != null) e.setWardType(req.getWardType());
        if (req.getFloorLocation() != null) e.setFloorLocation(req.getFloorLocation());
        if (req.getBedCount() != null) e.setBedCount(req.getBedCount());
        if (req.getNurseStation() != null) e.setNurseStation(req.getNurseStation());
        if (req.getHeadNurseId() != null) e.setHeadNurseId(req.getHeadNurseId());
        if (req.getWardStatus() != null) e.setWardStatus(req.getWardStatus());
        if (req.getDescription() != null) e.setDescription(req.getDescription());
    }

    public static WardVo toVo(Ward e) {
        WardVo vo = new WardVo();
        vo.setId(e.getId());
        vo.setWardCode(e.getWardCode());
        vo.setWardName(e.getWardName());
        vo.setDeptId(e.getDeptId());
        vo.setWardType(e.getWardType());
        vo.setFloorLocation(e.getFloorLocation());
        vo.setBedCount(e.getBedCount());
        vo.setNurseStation(e.getNurseStation());
        vo.setHeadNurseId(e.getHeadNurseId());
        vo.setWardStatus(e.getWardStatus());
        vo.setDescription(e.getDescription());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }
}
