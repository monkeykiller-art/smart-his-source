package com.smarthis.resource.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.BedAdmitRequest;
import com.smarthis.resource.dto.request.BedCreateRequest;
import com.smarthis.resource.dto.request.BedDischargeRequest;
import com.smarthis.resource.dto.request.BedQueryRequest;
import com.smarthis.resource.dto.request.BedUpdateRequest;
import com.smarthis.resource.dto.response.BedOverviewVo;
import com.smarthis.resource.dto.response.BedRecordVo;
import com.smarthis.resource.dto.response.BedVo;

import java.util.List;

public interface BedService {
    BedVo create(BedCreateRequest request);
    BedVo update(Long id, BedUpdateRequest request);
    void delete(Long id);
    BedVo getById(Long id);
    PageResult<BedVo> list(BedQueryRequest request);
    BedRecordVo admit(BedAdmitRequest request);
    BedRecordVo discharge(Long bedRecordId, BedDischargeRequest request);
    List<BedOverviewVo> overview(Long wardId);
}
