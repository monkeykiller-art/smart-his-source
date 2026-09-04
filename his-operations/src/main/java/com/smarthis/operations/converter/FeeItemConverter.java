package com.smarthis.operations.converter;

import com.smarthis.operations.dto.request.FeeItemCreateRequest;
import com.smarthis.operations.dto.response.FeeItemVo;
import com.smarthis.operations.entity.FeeItem;

public final class FeeItemConverter {

    private FeeItemConverter() {
    }

    public static FeeItem toEntity(FeeItemCreateRequest req) {
        FeeItem e = new FeeItem();
        e.setItemCode(req.getItemCode());
        e.setItemName(req.getItemName());
        e.setNamePinyin(req.getNamePinyin());
        e.setItemClass(req.getItemClass());
        e.setItemCategory(req.getItemCategory());
        e.setSpec(req.getSpec());
        e.setUnit(req.getUnit());
        e.setUnitPrice(req.getUnitPrice());
        e.setDosageForm(req.getDosageForm());
        e.setIsInsurance(req.getIsInsurance());
        e.setInsuranceRatio(req.getInsuranceRatio());
        e.setIsSelfPay(req.getIsSelfPay());
        e.setExecuteDeptType(req.getExecuteDeptType());
        e.setNeedConfirm(req.getNeedConfirm());
        e.setSortOrder(req.getSortOrder());
        e.setItemStatus(1);
        return e;
    }

    public static FeeItemVo toVo(FeeItem e) {
        FeeItemVo vo = new FeeItemVo();
        vo.setId(e.getId());
        vo.setItemCode(e.getItemCode());
        vo.setItemName(e.getItemName());
        vo.setNamePinyin(e.getNamePinyin());
        vo.setItemClass(e.getItemClass());
        vo.setItemCategory(e.getItemCategory());
        vo.setSpec(e.getSpec());
        vo.setUnit(e.getUnit());
        vo.setUnitPrice(e.getUnitPrice());
        vo.setDosageForm(e.getDosageForm());
        vo.setIsInsurance(e.getIsInsurance());
        vo.setInsuranceRatio(e.getInsuranceRatio());
        vo.setIsSelfPay(e.getIsSelfPay());
        vo.setExecuteDeptType(e.getExecuteDeptType());
        vo.setNeedConfirm(e.getNeedConfirm());
        vo.setSortOrder(e.getSortOrder());
        vo.setItemStatus(e.getItemStatus());
        return vo;
    }
}
