package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.CommonPhraseCreateRequest;
import com.smarthis.clinical.dto.request.CommonPhraseQueryRequest;
import com.smarthis.clinical.dto.request.CommonPhraseUpdateRequest;
import com.smarthis.clinical.dto.response.CommonPhraseVo;
import com.smarthis.common.model.PageResult;

public interface CommonPhraseService {
    CommonPhraseVo create(CommonPhraseCreateRequest request);
    CommonPhraseVo getById(Long id);
    PageResult<CommonPhraseVo> query(CommonPhraseQueryRequest request);
    CommonPhraseVo update(Long id, CommonPhraseUpdateRequest request);
    void delete(Long id);
}
