package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.clinical.converter.ExamRequestConverter;
import com.smarthis.clinical.dto.request.ExamRequestCreateRequest;
import com.smarthis.clinical.dto.request.ExamRequestItemRequest;
import com.smarthis.clinical.dto.request.ExamRequestResultRequest;
import com.smarthis.clinical.dto.response.ExamRequestVo;
import com.smarthis.clinical.entity.ExamRequest;
import com.smarthis.clinical.entity.ExamRequestItem;
import com.smarthis.clinical.mapper.ExamRequestItemMapper;
import com.smarthis.clinical.mapper.ExamRequestMapper;
import com.smarthis.clinical.mapper.MedicalRecordMapper;
import com.smarthis.clinical.entity.MedicalRecord;
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
    private final MedicalRecordMapper medicalRecordMapper;

    @Override
    @Transactional
    public ExamRequestVo create(ExamRequestCreateRequest request) {
        ExamRequest entity = ExamRequestConverter.toEntity(request);
        entity.setRequestNo(bizNoGenerator.next(BizNoType.EXAM_REQUEST));
        entity.setRequestTime(LocalDateTime.now());
        examRequestMapper.insert(entity);

        List<ExamRequestItem> items = new ArrayList<>();
        int seq = 1;
        for (ExamRequestItemRequest itemReq : request.getItems()) {
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
        transition(id, "CANCELLED");
    }

    @Override
    @Transactional
    public ExamRequestVo transition(Long id, String targetStatus) {
        ExamRequest entity = getEntity(id);
        String current = entity.getRequestStatus();
        boolean allowed = ("SUBMITTED".equals(current) && "ACCEPTED".equals(targetStatus))
                || ("ACCEPTED".equals(current) && "IN_PROGRESS".equals(targetStatus))
                || ("IN_PROGRESS".equals(current) && "REPORTED".equals(targetStatus))
                || (!"REPORTED".equals(current) && !"CANCELLED".equals(current) && "CANCELLED".equals(targetStatus));
        if (!allowed) throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        entity.setRequestStatus(targetStatus);
        if ("REPORTED".equals(targetStatus)) entity.setResultTime(LocalDateTime.now());
        examRequestMapper.updateById(entity);
        if (entity.getEncounterId() != null && request.getResultSummary() != null && !request.getResultSummary().isBlank()) {
            LambdaQueryWrapper<MedicalRecord> recordQuery = new LambdaQueryWrapper<>();
            recordQuery.eq(MedicalRecord::getEncounterId, entity.getEncounterId()).eq(MedicalRecord::getDeleted, 0)
                    .orderByDesc(MedicalRecord::getCreatedTime).last("LIMIT 1");
            MedicalRecord record = medicalRecordMapper.selectOne(recordQuery);
            if (record != null && !"SIGNED".equals(record.getRecordStatus()) && !"ARCHIVED".equals(record.getRecordStatus())) {
                String prefix = record.getAuxiliaryExam() == null || record.getAuxiliaryExam().isBlank() ? "" : record.getAuxiliaryExam() + "\n";
                record.setAuxiliaryExam(prefix + "检验检查[" + entity.getRequestNo() + "]: " + request.getResultSummary());
                medicalRecordMapper.updateById(record);
            }
        }
        return getById(id);
    }

    @Override
    @Transactional
    public ExamRequestVo report(Long id, ExamRequestResultRequest request) {
        ExamRequest entity = getEntity(id);
        if (!"IN_PROGRESS".equals(entity.getRequestStatus())) throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        entity.setResultSummary(request.getResultSummary());
        entity.setReportNo(request.getReportNo());
        entity.setIsCritical(request.getIsCritical() == null ? 0 : request.getIsCritical());
        entity.setReportUrl(request.getReportUrl());
        entity.setResultTime(LocalDateTime.now());
        entity.setRequestStatus("REPORTED");
        examRequestMapper.updateById(entity);
        return getById(id);
    }

    @Override
    @Transactional
    public ExamRequestVo acknowledgeCritical(Long id, Long userId) {
        ExamRequest entity = getEntity(id);
        if (!Integer.valueOf(1).equals(entity.getIsCritical())) throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        entity.setCriticalAcknowledged(1);
        entity.setCriticalAckBy(userId);
        entity.setCriticalAckTime(LocalDateTime.now());
        examRequestMapper.updateById(entity);
        return getById(id);
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
