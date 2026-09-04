package com.smarthis.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.clinical.converter.FrequencyDictConverter;
import com.smarthis.clinical.dto.request.FrequencyDictCreateRequest;
import com.smarthis.clinical.dto.request.FrequencyDictQueryRequest;
import com.smarthis.clinical.dto.request.FrequencyDictUpdateRequest;
import com.smarthis.clinical.dto.response.FrequencyDictVo;
import com.smarthis.clinical.entity.FrequencyDict;
import com.smarthis.clinical.mapper.FrequencyDictMapper;
import com.smarthis.clinical.service.FrequencyDictService;
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
public class FrequencyDictServiceImpl implements FrequencyDictService {

    private final FrequencyDictMapper frequencyDictMapper;

    @Override
    public FrequencyDictVo create(FrequencyDictCreateRequest request) {
        FrequencyDict entity = FrequencyDictConverter.toEntity(request);
        frequencyDictMapper.insert(entity);
        return FrequencyDictConverter.toVo(entity);
    }

    @Override
    public FrequencyDictVo getById(Long id) {
        FrequencyDict entity = frequencyDictMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return FrequencyDictConverter.toVo(entity);
    }

    @Override
    public PageResult<FrequencyDictVo> query(FrequencyDictQueryRequest request) {
        LambdaQueryWrapper<FrequencyDict> query = new LambdaQueryWrapper<>();
        query.eq(FrequencyDict::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword();
            query.and(w -> w.like(FrequencyDict::getFreqName, kw)
                    .or().like(FrequencyDict::getNamePinyin, kw)
                    .or().like(FrequencyDict::getFreqCode, kw));
        }
        if (request.getDailyTimes() != null) {
            query.eq(FrequencyDict::getDailyTimes, request.getDailyTimes());
        }
        if (request.getDictStatus() != null) {
            query.eq(FrequencyDict::getDictStatus, request.getDictStatus());
        }
        query.orderByAsc(FrequencyDict::getSortOrder);
        Page<FrequencyDict> page = request.toPage();
        IPage<FrequencyDict> result = frequencyDictMapper.selectPage(page, query);
        List<FrequencyDictVo> vos = result.getRecords().stream()
                .map(FrequencyDictConverter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public FrequencyDictVo update(Long id, FrequencyDictUpdateRequest request) {
        FrequencyDict entity = frequencyDictMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (request.getFreqCode() != null) entity.setFreqCode(request.getFreqCode());
        if (request.getFreqName() != null) entity.setFreqName(request.getFreqName());
        if (request.getNamePinyin() != null) entity.setNamePinyin(request.getNamePinyin());
        if (request.getDailyTimes() != null) entity.setDailyTimes(request.getDailyTimes());
        if (request.getFreqDesc() != null) entity.setFreqDesc(request.getFreqDesc());
        if (request.getSortOrder() != null) entity.setSortOrder(request.getSortOrder());
        if (request.getDictStatus() != null) entity.setDictStatus(request.getDictStatus());
        frequencyDictMapper.updateById(entity);
        return FrequencyDictConverter.toVo(entity);
    }
}
