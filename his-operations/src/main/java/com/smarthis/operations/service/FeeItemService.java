package com.smarthis.operations.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.FeeItemCreateRequest;
import com.smarthis.operations.dto.request.FeeItemQueryRequest;
import com.smarthis.operations.dto.request.FeeItemUpdateRequest;
import com.smarthis.operations.dto.response.FeeItemVo;

public interface FeeItemService {
    FeeItemVo create(FeeItemCreateRequest request);
    FeeItemVo getById(Long id);
    PageResult<FeeItemVo> query(FeeItemQueryRequest request);
    FeeItemVo update(Long id, FeeItemUpdateRequest request);
}
