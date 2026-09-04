package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DoseLimitCreateRequest;
import com.smarthis.pharma.dto.request.DoseLimitQueryRequest;
import com.smarthis.pharma.dto.response.DoseLimitVo;

public interface DoseLimitService {

    DoseLimitVo create(DoseLimitCreateRequest request);

    PageResult<DoseLimitVo> query(DoseLimitQueryRequest request);

    DoseLimitVo update(Long id, DoseLimitCreateRequest request);
}
