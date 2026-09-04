package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.DrugUsageDictCreateRequest;
import com.smarthis.clinical.dto.response.DrugUsageDictVo;
import com.smarthis.clinical.entity.DrugUsageDict;

public final class DrugUsageDictConverter {

    private DrugUsageDictConverter() {
    }

    public static DrugUsageDict toEntity(DrugUsageDictCreateRequest req) {
        DrugUsageDict e = new DrugUsageDict();
        e.setUsageCode(req.getUsageCode());
        e.setUsageName(req.getUsageName());
        e.setNamePinyin(req.getNamePinyin());
        e.setUsageDesc(req.getUsageDesc());
        e.setIsInjection(req.getIsInjection());
        e.setNeedSkinTest(req.getNeedSkinTest());
        e.setSortOrder(req.getSortOrder());
        e.setDictStatus(req.getDictStatus() != null ? req.getDictStatus() : 1);
        return e;
    }

    public static DrugUsageDictVo toVo(DrugUsageDict e) {
        DrugUsageDictVo vo = new DrugUsageDictVo();
        vo.setId(e.getId());
        vo.setUsageCode(e.getUsageCode());
        vo.setUsageName(e.getUsageName());
        vo.setNamePinyin(e.getNamePinyin());
        vo.setUsageDesc(e.getUsageDesc());
        vo.setIsInjection(e.getIsInjection());
        vo.setNeedSkinTest(e.getNeedSkinTest());
        vo.setSortOrder(e.getSortOrder());
        vo.setDictStatus(e.getDictStatus());
        return vo;
    }
}
