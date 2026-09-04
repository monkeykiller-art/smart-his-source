package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.clinical.converter.DrugUsageDictConverter;
import com.smarthis.clinical.dto.request.DrugUsageDictCreateRequest;
import com.smarthis.clinical.dto.request.DrugUsageDictQueryRequest;
import com.smarthis.clinical.dto.request.DrugUsageDictUpdateRequest;
import com.smarthis.clinical.dto.response.DrugUsageDictVo;
import com.smarthis.clinical.entity.DrugUsageDict;
import com.smarthis.clinical.mapper.DrugUsageDictMapper;
import com.smarthis.clinical.service.DrugUsageDictService;
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
public class DrugUsageDictServiceImpl implements DrugUsageDictService {

    private final DrugUsageDictMapper drugUsageDictMapper;

    @Override
    public DrugUsageDictVo create(DrugUsageDictCreateRequest request) {
        DrugUsageDict entity = DrugUsageDictConverter.toEntity(request);
        drugUsageDictMapper.insert(entity);
        return DrugUsageDictConverter.toVo(entity);
    }

    @Override
    public DrugUsageDictVo getById(Long id) {
        DrugUsageDict entity = drugUsageDictMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return DrugUsageDictConverter.toVo(entity);
    }

    @Override
    public PageResult<DrugUsageDictVo> query(DrugUsageDictQueryRequest request) {
        LambdaQueryWrapper<DrugUsageDict> query = new LambdaQueryWrapper<>();
        query.eq(DrugUsageDict::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword();
            query.and(w -> w.like(DrugUsageDict::getUsageName, kw)
                    .or().like(DrugUsageDict::getNamePinyin, kw)
                    .or().like(DrugUsageDict::getUsageCode, kw));
        }
        if (request.getIsInjection() != null) {
            query.eq(DrugUsageDict::getIsInjection, request.getIsInjection());
        }
        if (request.getDictStatus() != null) {
            query.eq(DrugUsageDict::getDictStatus, request.getDictStatus());
        }
        query.orderByAsc(DrugUsageDict::getSortOrder);
        Page<DrugUsageDict> page = request.toPage();
        IPage<DrugUsageDict> result = drugUsageDictMapper.selectPage(page, query);
        List<DrugUsageDictVo> vos = result.getRecords().stream()
                .map(DrugUsageDictConverter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public DrugUsageDictVo update(Long id, DrugUsageDictUpdateRequest request) {
        DrugUsageDict entity = drugUsageDictMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (request.getUsageCode() != null) entity.setUsageCode(request.getUsageCode());
        if (request.getUsageName() != null) entity.setUsageName(request.getUsageName());
        if (request.getNamePinyin() != null) entity.setNamePinyin(request.getNamePinyin());
        if (request.getUsageDesc() != null) entity.setUsageDesc(request.getUsageDesc());
        if (request.getIsInjection() != null) entity.setIsInjection(request.getIsInjection());
        if (request.getNeedSkinTest() != null) entity.setNeedSkinTest(request.getNeedSkinTest());
        if (request.getSortOrder() != null) entity.setSortOrder(request.getSortOrder());
        if (request.getDictStatus() != null) entity.setDictStatus(request.getDictStatus());
        drugUsageDictMapper.updateById(entity);
        return DrugUsageDictConverter.toVo(entity);
    }
}
