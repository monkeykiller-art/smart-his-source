package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.FrequencyDictCreateRequest;
import com.smarthis.clinical.dto.request.FrequencyDictQueryRequest;
import com.smarthis.clinical.dto.request.FrequencyDictUpdateRequest;
import com.smarthis.clinical.dto.response.FrequencyDictVo;
import com.smarthis.common.model.PageResult;

public interface FrequencyDictService {
    FrequencyDictVo create(FrequencyDictCreateRequest request);
    FrequencyDictVo getById(Long id);
    PageResult<FrequencyDictVo> query(FrequencyDictQueryRequest request);
    FrequencyDictVo update(Long id, FrequencyDictUpdateRequest request);
}
