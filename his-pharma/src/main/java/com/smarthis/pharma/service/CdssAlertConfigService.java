package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.CdssAlertConfigCreateRequest;
import com.smarthis.pharma.dto.request.CdssAlertConfigQueryRequest;
import com.smarthis.pharma.dto.request.CdssConfigUpdateRequest;
import com.smarthis.pharma.dto.response.CdssAlertConfigVo;

public interface CdssAlertConfigService {

    CdssAlertConfigVo create(CdssAlertConfigCreateRequest request);

    void update(Long id, CdssConfigUpdateRequest request);

    void delete(Long id);

    CdssAlertConfigVo getById(Long id);

    PageResult<CdssAlertConfigVo> list(CdssAlertConfigQueryRequest request);
}
