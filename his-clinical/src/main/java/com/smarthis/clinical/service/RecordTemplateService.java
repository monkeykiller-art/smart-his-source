package com.smarthis.clinical.service;

import com.smarthis.clinical.dto.request.RecordTemplateCreateRequest;
import com.smarthis.clinical.dto.request.RecordTemplateQueryRequest;
import com.smarthis.clinical.dto.request.RecordTemplateUpdateRequest;
import com.smarthis.clinical.dto.response.RecordTemplateVo;
import com.smarthis.common.model.PageResult;

public interface RecordTemplateService {
    RecordTemplateVo create(RecordTemplateCreateRequest request);
    RecordTemplateVo getById(Long id);
    PageResult<RecordTemplateVo> query(RecordTemplateQueryRequest request);
    RecordTemplateVo update(Long id, RecordTemplateUpdateRequest request);
    void delete(Long id);
}
