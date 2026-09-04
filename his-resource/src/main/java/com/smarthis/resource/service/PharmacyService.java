package com.smarthis.resource.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.PharmacyCreateRequest;
import com.smarthis.resource.dto.request.PharmacyQueryRequest;
import com.smarthis.resource.dto.request.PharmacyUpdateRequest;
import com.smarthis.resource.dto.response.PharmacyVo;

public interface PharmacyService {
    PharmacyVo create(PharmacyCreateRequest request);
    PharmacyVo update(Long id, PharmacyUpdateRequest request);
    void delete(Long id);
    PharmacyVo getById(Long id);
    PageResult<PharmacyVo> list(PharmacyQueryRequest request);
}
