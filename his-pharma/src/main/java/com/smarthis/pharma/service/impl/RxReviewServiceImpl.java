package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.pharma.converter.RxReviewConverter;
import com.smarthis.pharma.dto.request.RxReviewCreateRequest;
import com.smarthis.pharma.dto.request.RxReviewItemRequest;
import com.smarthis.pharma.dto.request.RxReviewQueryRequest;
import com.smarthis.pharma.dto.response.RxReviewItemVo;
import com.smarthis.pharma.dto.response.RxReviewVo;
import com.smarthis.pharma.entity.RxReview;
import com.smarthis.pharma.entity.RxReviewItem;
import com.smarthis.pharma.mapper.RxReviewItemMapper;
import com.smarthis.pharma.mapper.RxReviewMapper;
import com.smarthis.pharma.service.RxReviewService;
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
public class RxReviewServiceImpl implements RxReviewService {

    private final RxReviewMapper rxReviewMapper;
    private final RxReviewItemMapper rxReviewItemMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public RxReviewVo create(RxReviewCreateRequest request) {
        RxReview entity = RxReviewConverter.toEntity(request);
        entity.setReviewNo(bizNoGenerator.next(BizNoType.RX_REVIEW));
        rxReviewMapper.insert(entity);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            int warningCount = 0;
            int errorCount = 0;
            for (RxReviewItemRequest itemReq : request.getItems()) {
                RxReviewItem item = RxReviewConverter.toItemEntity(itemReq, entity.getId());
                rxReviewItemMapper.insert(item);
                if ("WARNING".equals(itemReq.getAlertLevel())) {
                    warningCount++;
                } else if ("ERROR".equals(itemReq.getAlertLevel()) || "FATAL".equals(itemReq.getAlertLevel())) {
                    errorCount++;
                }
            }
            entity.setWarningCount(warningCount);
            entity.setErrorCount(errorCount);

            if (errorCount > 0) {
                entity.setReviewResult("ERROR");
            } else if (warningCount > 0) {
                entity.setReviewResult("WARNING");
            } else {
                entity.setReviewResult("PASS");
            }
            entity.setReviewStatus("REVIEWING");
            rxReviewMapper.updateById(entity);
        }

        log.info("RxReview created: id={}, reviewNo={}, patientId={}", entity.getId(), entity.getReviewNo(), entity.getPatientId());

        RxReviewVo vo = RxReviewConverter.toVo(entity);
        vo.setItems(listItems(entity.getId()));
        return vo;
    }

    @Override
    public RxReviewVo getById(Long id) {
        RxReview review = getEntity(id);
        RxReviewVo vo = RxReviewConverter.toVo(review);
        vo.setItems(listItems(id));
        return vo;
    }

    @Override
    public PageResult<RxReviewVo> query(RxReviewQueryRequest request) {
        LambdaQueryWrapper<RxReview> query = new LambdaQueryWrapper<>();
        query.eq(RxReview::getDeleted, 0);
        if (StringUtils.hasText(request.getReviewStatus())) {
            query.eq(RxReview::getReviewStatus, request.getReviewStatus());
        }
        if (request.getDoctorId() != null) {
            query.eq(RxReview::getDoctorId, request.getDoctorId());
        }
        if (request.getPatientId() != null) {
            query.eq(RxReview::getPatientId, request.getPatientId());
        }
        query.orderByDesc(RxReview::getCreatedTime);

        Page<RxReview> page = request.toPage();
        IPage<RxReview> result = rxReviewMapper.selectPage(page, query);
        List<RxReviewVo> vos = result.getRecords().stream()
                .map(RxReviewConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public RxReviewVo approve(Long id, String reviewerId, String reviewerName) {
        RxReview review = getEntity(id);
        if ("REJECTED".equals(review.getReviewStatus())) {
            throw new BusinessException(ErrorCode.RX_REVIEW_REJECTED);
        }
        review.setReviewStatus("APPROVED");
        if (review.getReviewResult() == null) {
            review.setReviewResult("PASS");
        }
        review.setReviewerId(reviewerId);
        review.setReviewerName(reviewerName);
        review.setReviewTime(LocalDateTime.now());
        rxReviewMapper.updateById(review);
        log.info("RxReview approved: id={}, reviewer={}", id, reviewerName);

        RxReviewVo vo = RxReviewConverter.toVo(review);
        vo.setItems(listItems(id));
        return vo;
    }

    @Override
    @Transactional
    public RxReviewVo reject(Long id, String reviewerId, String reviewerName, String rejectReason) {
        RxReview review = getEntity(id);
        review.setReviewStatus("REJECTED");
        review.setReviewResult("ERROR");
        review.setRejectReason(rejectReason);
        review.setReviewerId(reviewerId);
        review.setReviewerName(reviewerName);
        review.setReviewTime(LocalDateTime.now());
        rxReviewMapper.updateById(review);
        log.info("RxReview rejected: id={}, reviewer={}, reason={}", id, reviewerName, rejectReason);

        RxReviewVo vo = RxReviewConverter.toVo(review);
        vo.setItems(listItems(id));
        return vo;
    }

    private List<RxReviewItemVo> listItems(Long reviewId) {
        LambdaQueryWrapper<RxReviewItem> query = new LambdaQueryWrapper<>();
        query.eq(RxReviewItem::getReviewId, reviewId)
                .eq(RxReviewItem::getDeleted, 0)
                .orderByAsc(RxReviewItem::getId);
        return rxReviewItemMapper.selectList(query).stream()
                .map(RxReviewConverter::toItemVo)
                .toList();
    }

    private RxReview getEntity(Long id) {
        RxReview review = rxReviewMapper.selectById(id);
        if (review == null || review.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return review;
    }
}
