package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugInteractionCreateRequest;
import com.smarthis.pharma.dto.request.DrugInteractionQueryRequest;
import com.smarthis.pharma.dto.response.DrugInteractionVo;

import java.util.List;

public interface DrugInteractionService {

    DrugInteractionVo create(DrugInteractionCreateRequest request);

    void delete(Long id);

    List<DrugInteractionVo> listByDrugCode(String drugCode);

    PageResult<DrugInteractionVo> list(DrugInteractionQueryRequest request);
}
