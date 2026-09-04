package com.smarthis.operations.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.FeeTemplateCreateRequest;
import com.smarthis.operations.dto.request.FeeTemplateQueryRequest;
import com.smarthis.operations.dto.request.FeeTemplateUpdateRequest;
import com.smarthis.operations.dto.response.FeeTemplateVo;

public interface FeeTemplateService {
    FeeTemplateVo create(FeeTemplateCreateRequest request);
    FeeTemplateVo getById(Long id);
    PageResult<FeeTemplateVo> query(FeeTemplateQueryRequest request);
    FeeTemplateVo update(Long id, FeeTemplateUpdateRequest request);
}
