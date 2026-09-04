package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.RxReviewCreateRequest;
import com.smarthis.pharma.dto.request.RxReviewQueryRequest;
import com.smarthis.pharma.dto.request.RxReviewRejectRequest;
import com.smarthis.pharma.dto.response.RxReviewVo;

public interface RxReviewService {

    RxReviewVo create(RxReviewCreateRequest request);

    RxReviewVo approve(Long id, String reviewerId, String reviewerName);

    RxReviewVo reject(Long id, String reviewerId, String reviewerName, String rejectReason);

    RxReviewVo getById(Long id);

    PageResult<RxReviewVo> query(RxReviewQueryRequest request);
}
