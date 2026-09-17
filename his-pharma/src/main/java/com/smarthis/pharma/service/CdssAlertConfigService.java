package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.CdssAlertConfigCreateRequest;
import com.smarthis.pharma.dto.request.CdssAlertConfigQueryRequest;
import com.smarthis.pharma.dto.response.CdssAlertConfigVo;

public interface CdssAlertConfigService {

    CdssAlertConfigVo create(CdssAlertConfigCreateRequest request);

    CdssAlertConfigVo getById(Long id);

    PageResult<CdssAlertConfigVo> query(CdssAlertConfigQueryRequest request);

    CdssAlertConfigVo update(Long id, CdssAlertConfigCreateRequest request);

    CdssAlertConfigVo toggle(Long id);
}
