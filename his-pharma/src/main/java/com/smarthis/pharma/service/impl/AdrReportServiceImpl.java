package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.pharma.converter.AdrReportConverter;
import com.smarthis.pharma.dto.request.AdrReportCreateRequest;
import com.smarthis.pharma.dto.request.AdrReportQueryRequest;
import com.smarthis.pharma.dto.request.AdrReportReviewRequest;
import com.smarthis.pharma.dto.response.AdrReportVo;
import com.smarthis.pharma.entity.AdrReport;
import com.smarthis.pharma.mapper.AdrReportMapper;
import com.smarthis.pharma.service.AdrReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdrReportServiceImpl implements AdrReportService {

    private final AdrReportMapper adrReportMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public AdrReportVo create(AdrReportCreateRequest request) {
        AdrReport entity = AdrReportConverter.toEntity(request);
        entity.setReportNo(bizNoGenerator.next(BizNoType.ADR_REPORT));
        entity.setReportTime(LocalDateTime.now());
        adrReportMapper.insert(entity);
        log.info("AdrReport created: id={}, reportNo={}, drugCode={}", entity.getId(), entity.getReportNo(), entity.getDrugCode());
        return AdrReportConverter.toVo(entity);
    }

    @Override
    public AdrReportVo getById(Long id) {
        AdrReport report = getEntity(id);
        return AdrReportConverter.toVo(report);
    }

    @Override
    public PageResult<AdrReportVo> query(AdrReportQueryRequest request) {
        LambdaQueryWrapper<AdrReport> query = new LambdaQueryWrapper<>();
        query.eq(AdrReport::getDeleted, 0);
        if (StringUtils.hasText(request.getReportStatus())) {
            query.eq(AdrReport::getReportStatus, request.getReportStatus());
        }
        if (StringUtils.hasText(request.getDrugCode())) {
            query.eq(AdrReport::getDrugCode, request.getDrugCode());
        }
        if (request.getPatientId() != null) {
            query.eq(AdrReport::getPatientId, request.getPatientId());
        }
        query.orderByDesc(AdrReport::getCreatedTime);

        Page<AdrReport> page = request.toPage();
        IPage<AdrReport> result = adrReportMapper.selectPage(page, query);
        List<AdrReportVo> vos = result.getRecords().stream()
                .map(AdrReportConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public AdrReportVo review(Long id, AdrReportReviewRequest request) {
        AdrReport report = getEntity(id);
        if (!"SUBMITTED".equals(report.getReportStatus()) && !"UNDER_REVIEW".equals(report.getReportStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        report.setReportStatus("REVIEWED");
        report.setReviewComment(request.getReviewComment());
        report.setReviewerId(request.getReviewerId());
        report.setReviewTime(LocalDateTime.now());
        adrReportMapper.updateById(report);
        log.info("AdrReport reviewed: id={}, reviewer={}", id, request.getReviewerId());
        return AdrReportConverter.toVo(report);
    }

    @Override
    @Transactional
    public AdrReportVo reportToAuthority(Long id) {
        AdrReport report = getEntity(id);
        if (!"REVIEWED".equals(report.getReportStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        report.setReportStatus("REPORTED_TO_AUTHORITY");
        report.setIsReportedToAuthority(1);
        adrReportMapper.updateById(report);
        log.info("AdrReport reported to authority: id={}", id);
        return AdrReportConverter.toVo(report);
    }

    private AdrReport getEntity(Long id) {
        AdrReport report = adrReportMapper.selectById(id);
        if (report == null || report.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return report;
    }
}
