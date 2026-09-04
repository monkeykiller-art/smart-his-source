package com.smarthis.operations.service;

import com.smarthis.operations.dto.request.BedFeeBindCreateRequest;
import com.smarthis.operations.dto.request.BedFeeBindUpdateRequest;
import com.smarthis.operations.dto.response.BedFeeBindVo;

import java.util.List;

public interface BedFeeBindService {
    BedFeeBindVo create(BedFeeBindCreateRequest request);
    List<BedFeeBindVo> queryByBedId(Long bedId);
    BedFeeBindVo update(Long id, BedFeeBindUpdateRequest request);
    void delete(Long id);
}
