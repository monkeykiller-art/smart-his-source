package com.smarthis.resource.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.DispenseCreateRequest;
import com.smarthis.resource.dto.request.DispenseQueryRequest;
import com.smarthis.resource.dto.response.DispenseVo;

public interface DispenseService {
    DispenseVo create(DispenseCreateRequest request);
    DispenseVo getById(Long id);
    PageResult<DispenseVo> list(DispenseQueryRequest request);
    DispenseVo review(Long id, String reviewerId);
}
