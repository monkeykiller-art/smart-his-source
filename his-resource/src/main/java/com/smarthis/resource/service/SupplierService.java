package com.smarthis.resource.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.SupplierCreateRequest;
import com.smarthis.resource.dto.request.SupplierQueryRequest;
import com.smarthis.resource.dto.request.SupplierUpdateRequest;
import com.smarthis.resource.dto.response.SupplierVo;

public interface SupplierService {
    SupplierVo create(SupplierCreateRequest request);
    SupplierVo update(Long id, SupplierUpdateRequest request);
    void delete(Long id);
    SupplierVo getById(Long id);
    PageResult<SupplierVo> list(SupplierQueryRequest request);
}
