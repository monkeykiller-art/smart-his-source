package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugContraindicationCreateRequest;
import com.smarthis.pharma.dto.request.DrugContraindicationQueryRequest;
import com.smarthis.pharma.dto.response.DrugContraindicationVo;

import java.util.List;

public interface DrugContraindicationService {

    DrugContraindicationVo create(DrugContraindicationCreateRequest request);

    void delete(Long id);

    List<DrugContraindicationVo> listByDrugCode(String drugCode);

    PageResult<DrugContraindicationVo> list(DrugContraindicationQueryRequest request);
}
