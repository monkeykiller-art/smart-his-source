package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugCatalogQueryRequest;
import com.smarthis.pharma.dto.request.DrugCatalogSaveRequest;
import com.smarthis.pharma.dto.response.DrugCatalogVo;

public interface DrugCatalogService {
    DrugCatalogVo create(DrugCatalogSaveRequest request);
    DrugCatalogVo update(Long id, DrugCatalogSaveRequest request);
    DrugCatalogVo getById(Long id);
    PageResult<DrugCatalogVo> query(DrugCatalogQueryRequest request);
    DrugCatalogVo setActive(Long id, boolean active);
}
