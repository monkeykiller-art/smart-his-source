package com.smarthis.resource.converter;

import com.smarthis.resource.dto.request.DrugPriceCreateRequest;
import com.smarthis.resource.dto.response.DrugPriceVo;
import com.smarthis.resource.entity.DrugPrice;

public final class DrugPriceConverter {

    private DrugPriceConverter() {
    }

    public static DrugPrice toEntity(DrugPriceCreateRequest req) {
        DrugPrice e = new DrugPrice();
        e.setDrugId(req.getDrugId());
        e.setPharmacyId(req.getPharmacyId());
        e.setPrice(req.getPrice());
        e.setRetailPrice(req.getRetailPrice());
        e.setIsActive(req.getIsActive() != null ? req.getIsActive() : 1);
        e.setEffectiveFrom(req.getEffectiveFrom());
        e.setEffectiveTo(req.getEffectiveTo());
        return e;
    }

    public static DrugPriceVo toVo(DrugPrice e) {
        DrugPriceVo vo = new DrugPriceVo();
        vo.setId(e.getId());
        vo.setDrugId(e.getDrugId());
        vo.setPharmacyId(e.getPharmacyId());
        vo.setPrice(e.getPrice());
        vo.setRetailPrice(e.getRetailPrice());
        vo.setIsActive(e.getIsActive());
        vo.setEffectiveFrom(e.getEffectiveFrom());
        vo.setEffectiveTo(e.getEffectiveTo());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }
}
