package com.smarthis.pharma.converter;

import com.smarthis.pharma.dto.request.DrugInteractionCreateRequest;
import com.smarthis.pharma.dto.response.DrugInteractionVo;
import com.smarthis.pharma.entity.DrugInteraction;

public final class DrugInteractionConverter {

    private DrugInteractionConverter() {
    }

    public static DrugInteraction toEntity(DrugInteractionCreateRequest req) {
        DrugInteraction e = new DrugInteraction();
        e.setDrugCodeA(req.getDrugCodeA());
        e.setDrugCodeB(req.getDrugCodeB());
        e.setInteractionLevel(req.getInteractionLevel());
        e.setInteractionDesc(req.getInteractionDesc());
        e.setSuggestion(req.getSuggestion());
        e.setReference(req.getReference());
        e.setIsActive(1);
        return e;
    }

    public static DrugInteractionVo toVo(DrugInteraction e) {
        DrugInteractionVo vo = new DrugInteractionVo();
        vo.setId(e.getId());
        vo.setDrugCodeA(e.getDrugCodeA());
        vo.setDrugCodeB(e.getDrugCodeB());
        vo.setInteractionLevel(e.getInteractionLevel());
        vo.setInteractionDesc(e.getInteractionDesc());
        vo.setSuggestion(e.getSuggestion());
        vo.setReference(e.getReference());
        vo.setIsActive(e.getIsActive());
        return vo;
    }
}
