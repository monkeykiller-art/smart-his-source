package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.CommonPhraseCreateRequest;
import com.smarthis.clinical.dto.response.CommonPhraseVo;
import com.smarthis.clinical.entity.CommonPhrase;

public final class CommonPhraseConverter {

    private CommonPhraseConverter() {
    }

    public static CommonPhrase toEntity(CommonPhraseCreateRequest req) {
        CommonPhrase e = new CommonPhrase();
        e.setPhraseName(req.getPhraseName());
        e.setPhraseContent(req.getPhraseContent());
        e.setPhraseType(req.getPhraseType());
        e.setDeptId(req.getDeptId());
        e.setUserId(req.getUserId());
        e.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        return e;
    }

    public static CommonPhraseVo toVo(CommonPhrase e) {
        CommonPhraseVo vo = new CommonPhraseVo();
        vo.setId(e.getId());
        vo.setPhraseName(e.getPhraseName());
        vo.setPhraseContent(e.getPhraseContent());
        vo.setPhraseType(e.getPhraseType());
        vo.setDeptId(e.getDeptId());
        vo.setUserId(e.getUserId());
        vo.setSortOrder(e.getSortOrder());
        return vo;
    }
}
