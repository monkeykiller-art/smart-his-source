package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.DrugUsageDictCreateRequest;
import com.smarthis.clinical.dto.request.DrugUsageDictQueryRequest;
import com.smarthis.clinical.dto.request.DrugUsageDictUpdateRequest;
import com.smarthis.clinical.dto.response.DrugUsageDictVo;
import com.smarthis.common.model.PageResult;

public interface DrugUsageDictService {
    DrugUsageDictVo create(DrugUsageDictCreateRequest request);
    DrugUsageDictVo getById(Long id);
    PageResult<DrugUsageDictVo> query(DrugUsageDictQueryRequest request);
    DrugUsageDictVo update(Long id, DrugUsageDictUpdateRequest request);
}
