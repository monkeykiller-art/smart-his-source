package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.clinical.converter.CommonPhraseConverter;
import com.smarthis.clinical.dto.request.CommonPhraseCreateRequest;
import com.smarthis.clinical.dto.request.CommonPhraseQueryRequest;
import com.smarthis.clinical.dto.request.CommonPhraseUpdateRequest;
import com.smarthis.clinical.dto.response.CommonPhraseVo;
import com.smarthis.clinical.entity.CommonPhrase;
import com.smarthis.clinical.mapper.CommonPhraseMapper;
import com.smarthis.clinical.service.CommonPhraseService;
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
public class CommonPhraseServiceImpl implements CommonPhraseService {

    private final CommonPhraseMapper commonPhraseMapper;

    @Override
    public CommonPhraseVo create(CommonPhraseCreateRequest request) {
        CommonPhrase entity = CommonPhraseConverter.toEntity(request);
        commonPhraseMapper.insert(entity);
        return CommonPhraseConverter.toVo(entity);
    }

    @Override
    public CommonPhraseVo getById(Long id) {
        CommonPhrase entity = commonPhraseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return CommonPhraseConverter.toVo(entity);
    }

    @Override
    public PageResult<CommonPhraseVo> query(CommonPhraseQueryRequest request) {
        LambdaQueryWrapper<CommonPhrase> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getPhraseType())) {
            query.eq(CommonPhrase::getPhraseType, request.getPhraseType());
        }
        if (request.getDeptId() != null) {
            query.eq(CommonPhrase::getDeptId, request.getDeptId());
        }
        if (request.getUserId() != null) {
            query.eq(CommonPhrase::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            query.and(w -> w.like(CommonPhrase::getPhraseName, request.getKeyword())
                    .or().like(CommonPhrase::getPhraseContent, request.getKeyword()));
        }
        query.orderByAsc(CommonPhrase::getSortOrder);
        Page<CommonPhrase> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<CommonPhrase> result = commonPhraseMapper.selectPage(page, query);
        List<CommonPhraseVo> vos = result.getRecords().stream()
                .map(CommonPhraseConverter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public CommonPhraseVo update(Long id, CommonPhraseUpdateRequest request) {
        CommonPhrase entity = commonPhraseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (request.getPhraseName() != null) entity.setPhraseName(request.getPhraseName());
        if (request.getPhraseContent() != null) entity.setPhraseContent(request.getPhraseContent());
        if (request.getPhraseType() != null) entity.setPhraseType(request.getPhraseType());
        if (request.getDeptId() != null) entity.setDeptId(request.getDeptId());
        if (request.getUserId() != null) entity.setUserId(request.getUserId());
        if (request.getSortOrder() != null) entity.setSortOrder(request.getSortOrder());
        commonPhraseMapper.updateById(entity);
        return CommonPhraseConverter.toVo(entity);
    }

    @Override
    public void delete(Long id) {
        CommonPhrase entity = commonPhraseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        commonPhraseMapper.deleteById(id);
    }
}
