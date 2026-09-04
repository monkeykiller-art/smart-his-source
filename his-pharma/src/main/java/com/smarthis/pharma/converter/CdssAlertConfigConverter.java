package com.smarthis.pharma.converter;

import com.smarthis.pharma.dto.request.CdssAlertConfigCreateRequest;
import com.smarthis.pharma.dto.request.CdssConfigUpdateRequest;
import com.smarthis.pharma.dto.response.CdssAlertConfigVo;
import com.smarthis.pharma.entity.CdssAlertConfig;

public final class CdssAlertConfigConverter {

    private CdssAlertConfigConverter() {
    }

    public static CdssAlertConfig toEntity(CdssAlertConfigCreateRequest req) {
        CdssAlertConfig e = new CdssAlertConfig();
        e.setAlertType(req.getAlertType());
        e.setAlertName(req.getAlertName());
        e.setAlertLevel(req.getAlertLevel());
        e.setIsEnabled(1);
        e.setDeptId(req.getDeptId());
        e.setDescription(req.getDescription());
        e.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        return e;
    }

    public static void applyUpdate(CdssAlertConfig e, CdssConfigUpdateRequest req) {
        if (req.getAlertName() != null) {
            e.setAlertName(req.getAlertName());
        }
        if (req.getAlertLevel() != null) {
            e.setAlertLevel(req.getAlertLevel());
        }
        if (req.getIsEnabled() != null) {
            e.setIsEnabled(req.getIsEnabled());
        }
        if (req.getDescription() != null) {
            e.setDescription(req.getDescription());
        }
        if (req.getSortOrder() != null) {
            e.setSortOrder(req.getSortOrder());
        }
    }

    public static CdssAlertConfigVo toVo(CdssAlertConfig e) {
        CdssAlertConfigVo vo = new CdssAlertConfigVo();
        vo.setId(e.getId());
        vo.setAlertType(e.getAlertType());
        vo.setAlertName(e.getAlertName());
        vo.setAlertLevel(e.getAlertLevel());
        vo.setIsEnabled(e.getIsEnabled());
        vo.setDeptId(e.getDeptId());
        vo.setDescription(e.getDescription());
        vo.setSortOrder(e.getSortOrder());
        return vo;
    }
}
