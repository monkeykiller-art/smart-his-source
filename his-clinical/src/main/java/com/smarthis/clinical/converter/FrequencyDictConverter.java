package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.FrequencyDictCreateRequest;
import com.smarthis.clinical.dto.response.FrequencyDictVo;
import com.smarthis.clinical.entity.FrequencyDict;

public final class FrequencyDictConverter {

    private FrequencyDictConverter() {
    }

    public static FrequencyDict toEntity(FrequencyDictCreateRequest req) {
        FrequencyDict e = new FrequencyDict();
        e.setFreqCode(req.getFreqCode());
        e.setFreqName(req.getFreqName());
        e.setNamePinyin(req.getNamePinyin());
        e.setDailyTimes(req.getDailyTimes());
        e.setFreqDesc(req.getFreqDesc());
        e.setSortOrder(req.getSortOrder());
        e.setDictStatus(req.getDictStatus() != null ? req.getDictStatus() : 1);
        return e;
    }

    public static FrequencyDictVo toVo(FrequencyDict e) {
        FrequencyDictVo vo = new FrequencyDictVo();
        vo.setId(e.getId());
        vo.setFreqCode(e.getFreqCode());
        vo.setFreqName(e.getFreqName());
        vo.setNamePinyin(e.getNamePinyin());
        vo.setDailyTimes(e.getDailyTimes());
        vo.setFreqDesc(e.getFreqDesc());
        vo.setSortOrder(e.getSortOrder());
        vo.setDictStatus(e.getDictStatus());
        return vo;
    }
}
