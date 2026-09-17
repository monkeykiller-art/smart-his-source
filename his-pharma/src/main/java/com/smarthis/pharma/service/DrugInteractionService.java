package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugInteractionCreateRequest;
import com.smarthis.pharma.dto.request.DrugInteractionCheckRequest;
import com.smarthis.pharma.dto.request.DrugInteractionQueryRequest;
import com.smarthis.pharma.dto.response.DrugInteractionVo;

import java.util.List;

public interface DrugInteractionService {

    DrugInteractionVo create(DrugInteractionCreateRequest request);

    PageResult<DrugInteractionVo> query(DrugInteractionQueryRequest request);

    DrugInteractionVo update(Long id, DrugInteractionCreateRequest request);

    List<DrugInteractionVo> check(DrugInteractionCheckRequest request);
}
