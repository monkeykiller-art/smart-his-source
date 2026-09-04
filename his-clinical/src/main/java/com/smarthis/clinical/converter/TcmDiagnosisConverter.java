package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.response.TcmDiagnosisVo;
import com.smarthis.clinical.entity.TcmDiagnosis;

public final class TcmDiagnosisConverter {

    private TcmDiagnosisConverter() {
    }

    public static TcmDiagnosisVo toVo(TcmDiagnosis e) {
        TcmDiagnosisVo vo = new TcmDiagnosisVo();
        vo.setId(e.getId());
        vo.setTcmCode(e.getTcmCode());
        vo.setTcmName(e.getTcmName());
        vo.setNamePinyin(e.getNamePinyin());
        vo.setSyndromeCode(e.getSyndromeCode());
        vo.setSyndromeName(e.getSyndromeName());
        vo.setCategory(e.getCategory());
        vo.setSortOrder(e.getSortOrder());
        vo.setDictStatus(e.getDictStatus());
        return vo;
    }
}
