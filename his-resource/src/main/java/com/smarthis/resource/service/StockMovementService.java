package com.smarthis.resource.service;

import com.smarthis.resource.dto.request.StockMovementCreateRequest;
import com.smarthis.resource.dto.response.StockMovementVo;

import java.util.List;

public interface StockMovementService {
    StockMovementVo create(StockMovementCreateRequest request);
    List<StockMovementVo> listByDrug(Long drugId, Long pharmacyId);
}
