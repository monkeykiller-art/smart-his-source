package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.clinical.converter.Icd10Converter;
import com.smarthis.clinical.dto.request.Icd10QueryRequest;
import com.smarthis.clinical.dto.response.Icd10Vo;
import com.smarthis.clinical.entity.Icd10;
import com.smarthis.clinical.mapper.Icd10Mapper;
import com.smarthis.clinical.service.Icd10Service;
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
public class Icd10ServiceImpl implements Icd10Service {

    private final Icd10Mapper icd10Mapper;

    @Override
    public Icd10Vo getById(Long id) {
        Icd10 entity = icd10Mapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.ICD10_NOT_FOUND);
        }
        return Icd10Converter.toVo(entity);
    }

    @Override
    public PageResult<Icd10Vo> query(Icd10QueryRequest request) {
        LambdaQueryWrapper<Icd10> query = new LambdaQueryWrapper<>();
        query.eq(Icd10::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword();
            query.and(w -> w.like(Icd10::getIcdName, kw)
                    .or().like(Icd10::getNamePinyin, kw)
                    .or().like(Icd10::getIcdCode, kw));
        }
        if (StringUtils.hasText(request.getChapter())) {
            query.eq(Icd10::getChapter, request.getChapter());
        }
        if (StringUtils.hasText(request.getCategory())) {
            query.eq(Icd10::getCategory, request.getCategory());
        }
        if (request.getDictStatus() != null) {
            query.eq(Icd10::getDictStatus, request.getDictStatus());
        }
        query.orderByAsc(Icd10::getSortOrder);
        Page<Icd10> page = request.toPage();
        IPage<Icd10> result = icd10Mapper.selectPage(page, query);
        List<Icd10Vo> vos = result.getRecords().stream().map(Icd10Converter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }
}
