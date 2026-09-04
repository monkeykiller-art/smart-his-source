package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.DoseLimitCreateRequest;
import com.smarthis.pharma.dto.request.DoseLimitQueryRequest;
import com.smarthis.pharma.dto.response.DoseLimitVo;

import java.util.List;

public interface DoseLimitService {

    DoseLimitVo create(DoseLimitCreateRequest request);

    void delete(Long id);

    List<DoseLimitVo> listByDrugCode(String drugCode);

    PageResult<DoseLimitVo> list(DoseLimitQueryRequest request);
}
