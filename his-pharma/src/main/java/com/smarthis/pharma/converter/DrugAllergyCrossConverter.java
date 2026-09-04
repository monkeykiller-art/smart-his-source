package com.smarthis.pharma.converter;

import com.smarthis.pharma.dto.request.DrugAllergyCrossCreateRequest;
import com.smarthis.pharma.dto.response.DrugAllergyCrossVo;
import com.smarthis.pharma.entity.DrugAllergyCross;

public final class DrugAllergyCrossConverter {

    private DrugAllergyCrossConverter() {
    }

    public static DrugAllergyCross toEntity(DrugAllergyCrossCreateRequest req) {
        DrugAllergyCross e = new DrugAllergyCross();
        e.setAllergyCode(req.getAllergyCode());
        e.setAllergyName(req.getAllergyName());
        e.setCrossDrugCode(req.getCrossDrugCode());
        e.setCrossDrugName(req.getCrossDrugName());
        e.setCrossLevel(req.getCrossLevel());
        e.setDescription(req.getDescription());
        e.setIsActive(1);
        return e;
    }

    public static DrugAllergyCrossVo toVo(DrugAllergyCross e) {
        DrugAllergyCrossVo vo = new DrugAllergyCrossVo();
        vo.setId(e.getId());
        vo.setAllergyCode(e.getAllergyCode());
        vo.setAllergyName(e.getAllergyName());
        vo.setCrossDrugCode(e.getCrossDrugCode());
        vo.setCrossDrugName(e.getCrossDrugName());
        vo.setCrossLevel(e.getCrossLevel());
        vo.setDescription(e.getDescription());
        vo.setIsActive(e.getIsActive());
        return vo;
    }
}
