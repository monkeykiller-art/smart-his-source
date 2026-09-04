package com.smarthis.resource.service;

import com.smarthis.resource.dto.request.DrugPriceCreateRequest;
import com.smarthis.resource.dto.response.DrugPriceVo;

import java.util.List;

public interface DrugPriceService {
    DrugPriceVo create(DrugPriceCreateRequest request);
    List<DrugPriceVo> listByDrug(Long drugId);
    List<DrugPriceVo> listByPharmacy(Long pharmacyId);
}
