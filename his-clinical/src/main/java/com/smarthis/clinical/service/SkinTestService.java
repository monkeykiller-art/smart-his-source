package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.SkinTestCreateRequest;
import com.smarthis.clinical.dto.request.SkinTestJudgeRequest;
import com.smarthis.clinical.dto.request.SkinTestQueryRequest;
import com.smarthis.clinical.dto.response.SkinTestVo;
import com.smarthis.common.model.PageResult;

public interface SkinTestService {
    SkinTestVo create(SkinTestCreateRequest request);
    SkinTestVo getById(Long id);
    PageResult<SkinTestVo> query(SkinTestQueryRequest request);
    SkinTestVo judge(Long id, SkinTestJudgeRequest request);
}
