package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.RxReviewApproveRequest;
import com.smarthis.pharma.dto.request.RxReviewCreateRequest;
import com.smarthis.pharma.dto.request.RxReviewQueryRequest;
import com.smarthis.pharma.dto.request.RxReviewRejectRequest;
import com.smarthis.pharma.dto.response.RxReviewVo;

public interface RxReviewService {

    RxReviewVo create(RxReviewCreateRequest request);

    void approve(Long id, RxReviewApproveRequest request);

    void reject(Long id, RxReviewRejectRequest request);

    RxReviewVo getById(Long id);

    PageResult<RxReviewVo> list(RxReviewQueryRequest request);
}
