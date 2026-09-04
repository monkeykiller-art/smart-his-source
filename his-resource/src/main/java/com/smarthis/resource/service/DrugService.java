package com.smarthis.resource.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.DrugCreateRequest;
import com.smarthis.resource.dto.request.DrugQueryRequest;
import com.smarthis.resource.dto.request.DrugUpdateRequest;
import com.smarthis.resource.dto.response.DrugVo;

import java.util.List;

public interface DrugService {
    DrugVo create(DrugCreateRequest request);
    DrugVo update(Long id, DrugUpdateRequest request);
    void delete(Long id);
    DrugVo getById(Long id);
    PageResult<DrugVo> list(DrugQueryRequest request);
    List<DrugVo> search(String keyword);
}
