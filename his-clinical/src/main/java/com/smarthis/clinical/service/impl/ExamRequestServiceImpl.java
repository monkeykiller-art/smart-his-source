package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.clinical.converter.ExamRequestConverter;
import com.smarthis.clinical.dto.request.ExamRequestCreateRequest;
import com.smarthis.clinical.dto.response.ExamRequestVo;
import com.smarthis.clinical.entity.ExamRequest;
import com.smarthis.clinical.entity.ExamRequestItem;
import com.smarthis.clinical.mapper.ExamRequestItemMapper;
import com.smarthis.clinical.mapper.ExamRequestMapper;
import com.smarthis.clinical.service.ExamRequestService;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamRequestServiceImpl implements ExamRequestService {

    private final ExamRequestMapper examRequestMapper;
    private final ExamRequestItemMapper examRequestItemMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public ExamRequestVo create(ExamRequestCreateRequest request) {
        ExamRequest entity = ExamRequestConverter.toEntity(request);
        entity.setRequestNo(bizNoGenerator.next(BizNoType.EXAM_REQUEST));
        entity.setRequestTime(LocalDateTime.now());
        examRequestMapper.insert(entity);

        List<ExamRequestItem> items = new ArrayList<>();
        int seq = 1;
        for (ExamRequestCreateRequest.ExamRequestItemRequest itemReq : request.getItems()) {
            ExamRequestItem item = ExamRequestConverter.toItemEntity(entity.getId(), seq++, itemReq);
            examRequestItemMapper.insert(item);
            items.add(item);
        }

        log.info("Exam request created: requestNo={}, patientId={}", entity.getRequestNo(), entity.getPatientId());
        ExamRequestVo vo = ExamRequestConverter.toVo(entity);
        vo.setItems(items.stream().map(ExamRequestConverter::toItemVo).toList());
        return vo;
    }

    @Override
    public ExamRequestVo getById(Long id) {
        ExamRequest entity = getEntity(id);
        ExamRequestVo vo = ExamRequestConverter.toVo(entity);

        LambdaQueryWrapper<ExamRequestItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(ExamRequestItem::getRequestId, id)
                .eq(ExamRequestItem::getDeleted, 0)
                .orderByAsc(ExamRequestItem::getItemSeq);
        List<ExamRequestItem> items = examRequestItemMapper.selectList(itemQuery);
        vo.setItems(items.stream().map(ExamRequestConverter::toItemVo).toList());
        return vo;
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        ExamRequest entity = getEntity(id);
        if ("REPORTED".equals(entity.getRequestStatus()) || "CANCELLED".equals(entity.getRequestStatus())) {
            throw new BusinessException(ErrorCode.EXAM_REQUEST_NOT_FOUND);
        }
        entity.setRequestStatus("CANCELLED");
        examRequestMapper.updateById(entity);
        log.info("Exam request cancelled: id={}", id);
    }

    @Override
    public List<ExamRequestVo> listByPatient(Long patientId) {
        LambdaQueryWrapper<ExamRequest> query = new LambdaQueryWrapper<>();
        query.eq(ExamRequest::getPatientId, patientId)
                .eq(ExamRequest::getDeleted, 0)
                .orderByDesc(ExamRequest::getRequestTime);
        List<ExamRequest> requests = examRequestMapper.selectList(query);
        return requests.stream().map(r -> {
            ExamRequestVo vo = ExamRequestConverter.toVo(r);
            LambdaQueryWrapper<ExamRequestItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(ExamRequestItem::getRequestId, r.getId())
                    .eq(ExamRequestItem::getDeleted, 0)
                    .orderByAsc(ExamRequestItem::getItemSeq);
            List<ExamRequestItem> items = examRequestItemMapper.selectList(itemQuery);
            vo.setItems(items.stream().map(ExamRequestConverter::toItemVo).toList());
            return vo;
        }).toList();
    }

    @Override
    public List<ExamRequestVo> listByAdmission(Long admissionId) {
        LambdaQueryWrapper<ExamRequest> query = new LambdaQueryWrapper<>();
        query.eq(ExamRequest::getAdmissionId, admissionId)
                .eq(ExamRequest::getDeleted, 0)
                .orderByDesc(ExamRequest::getRequestTime);
        List<ExamRequest> requests = examRequestMapper.selectList(query);
        return requests.stream().map(r -> {
            ExamRequestVo vo = ExamRequestConverter.toVo(r);
            LambdaQueryWrapper<ExamRequestItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(ExamRequestItem::getRequestId, r.getId())
                    .eq(ExamRequestItem::getDeleted, 0)
                    .orderByAsc(ExamRequestItem::getItemSeq);
            List<ExamRequestItem> items = examRequestItemMapper.selectList(itemQuery);
            vo.setItems(items.stream().map(ExamRequestConverter::toItemVo).toList());
            return vo;
        }).toList();
    }

    private ExamRequest getEntity(Long id) {
        ExamRequest request = examRequestMapper.selectById(id);
        if (request == null || request.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.EXAM_REQUEST_NOT_FOUND);
        }
        return request;
    }
}
