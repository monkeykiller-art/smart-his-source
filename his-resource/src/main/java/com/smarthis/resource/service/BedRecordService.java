package com.smarthis.resource.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.resource.dto.request.BedRecordCreateRequest;
import com.smarthis.resource.dto.response.BedOverviewVo;
import com.smarthis.resource.dto.response.BedRecordVo;

import java.util.List;

public interface BedRecordService {

    BedRecordVo admit(BedRecordCreateRequest request);

    BedRecordVo getById(Long id);

    PageResult<BedRecordVo> query(Long wardId, String recordStatus, int page, int size);

    BedRecordVo discharge(Long id);

    List<BedOverviewVo> bedOverview(Long wardId);
}
