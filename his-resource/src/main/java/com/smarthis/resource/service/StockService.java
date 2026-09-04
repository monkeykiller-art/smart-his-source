package com.smarthis.resource.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.StockQueryRequest;
import com.smarthis.resource.dto.response.StockVo;

import java.util.List;

public interface StockService {
    PageResult<StockVo> list(StockQueryRequest request);
    List<StockVo> listByDrug(Long drugId, Long pharmacyId);
}
