package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugAllergyCrossCreateRequest;
import com.smarthis.pharma.dto.request.DrugAllergyCrossQueryRequest;
import com.smarthis.pharma.dto.response.DrugAllergyCrossVo;

public interface DrugAllergyCrossService {

    DrugAllergyCrossVo create(DrugAllergyCrossCreateRequest request);

    PageResult<DrugAllergyCrossVo> query(DrugAllergyCrossQueryRequest request);

    DrugAllergyCrossVo update(Long id, DrugAllergyCrossCreateRequest request);
}
