package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugContraindicationCreateRequest;
import com.smarthis.pharma.dto.request.DrugContraindicationQueryRequest;
import com.smarthis.pharma.dto.response.DrugContraindicationVo;

public interface DrugContraindicationService {

    DrugContraindicationVo create(DrugContraindicationCreateRequest request);

    PageResult<DrugContraindicationVo> query(DrugContraindicationQueryRequest request);

    DrugContraindicationVo update(Long id, DrugContraindicationCreateRequest request);
}
