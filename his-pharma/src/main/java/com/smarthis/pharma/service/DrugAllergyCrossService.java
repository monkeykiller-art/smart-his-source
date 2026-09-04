package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DrugAllergyCrossCreateRequest;
import com.smarthis.pharma.dto.request.DrugAllergyCrossQueryRequest;
import com.smarthis.pharma.dto.response.DrugAllergyCrossVo;

import java.util.List;

public interface DrugAllergyCrossService {

    DrugAllergyCrossVo create(DrugAllergyCrossCreateRequest request);

    void delete(Long id);

    List<DrugAllergyCrossVo> listByAllergyCode(String allergyCode);

    PageResult<DrugAllergyCrossVo> list(DrugAllergyCrossQueryRequest request);
}
