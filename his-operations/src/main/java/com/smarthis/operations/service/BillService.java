package com.smarthis.operations.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.operations.dto.request.BillChargeItemRequest;
import com.smarthis.operations.dto.request.BillChargeRequest;
import com.smarthis.operations.dto.request.BillCreateRequest;
import com.smarthis.operations.dto.request.BillQueryRequest;
import com.smarthis.operations.dto.request.BillRegistrationRequest;
import com.smarthis.operations.dto.response.BillItemVo;
import com.smarthis.operations.dto.response.BillVo;

import java.util.List;

public interface BillService {
    BillVo create(BillCreateRequest request);
    BillVo getById(Long id);
    PageResult<BillVo> query(BillQueryRequest request);
    BillVo createFromRegistration(BillRegistrationRequest request);
    List<BillItemVo> addChargeItem(Long billId, BillChargeItemRequest request);
    List<BillItemVo> chargeItems(BillChargeRequest request);
}
