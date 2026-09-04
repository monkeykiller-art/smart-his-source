package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.response.Icd10Vo;
import com.smarthis.clinical.entity.Icd10;

public final class Icd10Converter {

    private Icd10Converter() {
    }

    public static Icd10Vo toVo(Icd10 e) {
        Icd10Vo vo = new Icd10Vo();
        vo.setId(e.getId());
        vo.setIcdCode(e.getIcdCode());
        vo.setIcdName(e.getIcdName());
        vo.setNamePinyin(e.getNamePinyin());
        vo.setChapter(e.getChapter());
        vo.setBlock(e.getBlock());
        vo.setCategory(e.getCategory());
        vo.setSubCategory(e.getSubCategory());
        vo.setIsInfectious(e.getIsInfectious());
        vo.setIsChronic(e.getIsChronic());
        vo.setIsTcm(e.getIsTcm());
        vo.setDictStatus(e.getDictStatus());
        return vo;
    }
}
