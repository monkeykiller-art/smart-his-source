package com.smarthis.resource.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.WardCreateRequest;
import com.smarthis.resource.dto.request.WardQueryRequest;
import com.smarthis.resource.dto.request.WardUpdateRequest;
import com.smarthis.resource.dto.response.WardVo;

import java.util.List;

public interface WardService {
    WardVo create(WardCreateRequest request);
    WardVo update(Long id, WardUpdateRequest request);
    void delete(Long id);
    WardVo getById(Long id);
    PageResult<WardVo> list(WardQueryRequest request);
    List<WardVo> listAll();
}
