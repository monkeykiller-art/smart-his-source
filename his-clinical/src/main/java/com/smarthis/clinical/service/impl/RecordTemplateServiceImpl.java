package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.clinical.converter.RecordTemplateConverter;
import com.smarthis.clinical.dto.request.RecordTemplateCreateRequest;
import com.smarthis.clinical.dto.request.RecordTemplateQueryRequest;
import com.smarthis.clinical.dto.request.RecordTemplateUpdateRequest;
import com.smarthis.clinical.dto.response.RecordTemplateVo;
import com.smarthis.clinical.entity.RecordTemplate;
import com.smarthis.clinical.mapper.RecordTemplateMapper;
import com.smarthis.clinical.service.RecordTemplateService;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordTemplateServiceImpl implements RecordTemplateService {

    private final RecordTemplateMapper recordTemplateMapper;

    @Override
    public RecordTemplateVo create(RecordTemplateCreateRequest request) {
        RecordTemplate entity = RecordTemplateConverter.toEntity(request);
        recordTemplateMapper.insert(entity);
        return RecordTemplateConverter.toVo(entity);
    }

    @Override
    public RecordTemplateVo getById(Long id) {
        RecordTemplate entity = recordTemplateMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return RecordTemplateConverter.toVo(entity);
    }

    @Override
    public PageResult<RecordTemplateVo> query(RecordTemplateQueryRequest request) {
        LambdaQueryWrapper<RecordTemplate> query = new LambdaQueryWrapper<>();
        query.eq(RecordTemplate::getDeleted, 0);
        if (StringUtils.hasText(request.getTemplateName())) {
            query.like(RecordTemplate::getTemplateName, request.getTemplateName());
        }
        if (StringUtils.hasText(request.getTemplateType())) {
            query.eq(RecordTemplate::getTemplateType, request.getTemplateType());
        }
        if (request.getDeptId() != null) {
            query.eq(RecordTemplate::getDeptId, request.getDeptId());
        }
        if (StringUtils.hasText(request.getRecordType())) {
            query.eq(RecordTemplate::getRecordType, request.getRecordType());
        }
        if (request.getTemplateStatus() != null) {
            query.eq(RecordTemplate::getTemplateStatus, request.getTemplateStatus());
        }
        query.orderByAsc(RecordTemplate::getSortOrder);
        Page<RecordTemplate> page = request.toPage();
        IPage<RecordTemplate> result = recordTemplateMapper.selectPage(page, query);
        List<RecordTemplateVo> vos = result.getRecords().stream()
                .map(RecordTemplateConverter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public RecordTemplateVo update(Long id, RecordTemplateUpdateRequest request) {
        RecordTemplate entity = recordTemplateMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (request.getTemplateName() != null) entity.setTemplateName(request.getTemplateName());
        if (request.getTemplateType() != null) entity.setTemplateType(request.getTemplateType());
        if (request.getDeptId() != null) entity.setDeptId(request.getDeptId());
        if (request.getDiseaseCode() != null) entity.setDiseaseCode(request.getDiseaseCode());
        if (request.getRecordType() != null) entity.setRecordType(request.getRecordType());
        if (request.getTemplateContent() != null) entity.setTemplateContent(request.getTemplateContent());
        if (request.getSortOrder() != null) entity.setSortOrder(request.getSortOrder());
        if (request.getTemplateStatus() != null) entity.setTemplateStatus(request.getTemplateStatus());
        recordTemplateMapper.updateById(entity);
        return RecordTemplateConverter.toVo(entity);
    }

    @Override
    public void delete(Long id) {
        RecordTemplate entity = recordTemplateMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        entity.setDeleted(1);
        recordTemplateMapper.updateById(entity);
    }
}
