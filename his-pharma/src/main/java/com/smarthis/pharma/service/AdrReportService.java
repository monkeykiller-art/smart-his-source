package com.smarthis.pharma.service;

import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.dto.request.AdrReportCreateRequest;
import com.smarthis.pharma.dto.request.AdrReportQueryRequest;
import com.smarthis.pharma.dto.request.AdrReportReviewRequest;
import com.smarthis.pharma.dto.response.AdrReportVo;

public interface AdrReportService {

    AdrReportVo create(AdrReportCreateRequest request);

    void review(Long id, AdrReportReviewRequest request);

    AdrReportVo getById(Long id);

    PageResult<AdrReportVo> list(AdrReportQueryRequest request);
}
