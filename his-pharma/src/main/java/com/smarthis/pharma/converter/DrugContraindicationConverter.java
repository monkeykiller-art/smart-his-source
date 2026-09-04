package com.smarthis.pharma.converter;

import com.smarthis.pharma.dto.request.DrugContraindicationCreateRequest;
import com.smarthis.pharma.dto.response.DrugContraindicationVo;
import com.smarthis.pharma.entity.DrugContraindication;

public final class DrugContraindicationConverter {

    private DrugContraindicationConverter() {
    }

    public static DrugContraindication toEntity(DrugContraindicationCreateRequest req) {
        DrugContraindication e = new DrugContraindication();
        e.setDrugCode(req.getDrugCode());
        e.setContraindicationType(req.getContraindicationType());
        e.setContraindicationCode(req.getContraindicationCode());
        e.setContraindicationName(req.getContraindicationName());
        e.setSeverityLevel(req.getSeverityLevel());
        e.setDescription(req.getDescription());
        e.setSuggestion(req.getSuggestion());
        e.setIsActive(1);
        return e;
    }

    public static DrugContraindicationVo toVo(DrugContraindication e) {
        DrugContraindicationVo vo = new DrugContraindicationVo();
        vo.setId(e.getId());
        vo.setDrugCode(e.getDrugCode());
        vo.setContraindicationType(e.getContraindicationType());
        vo.setContraindicationCode(e.getContraindicationCode());
        vo.setContraindicationName(e.getContraindicationName());
        vo.setSeverityLevel(e.getSeverityLevel());
        vo.setDescription(e.getDescription());
        vo.setSuggestion(e.getSuggestion());
        vo.setIsActive(e.getIsActive());
        return vo;
    }
}
