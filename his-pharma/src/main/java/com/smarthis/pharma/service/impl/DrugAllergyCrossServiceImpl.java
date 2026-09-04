package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.converter.DrugAllergyCrossConverter;
import com.smarthis.pharma.dto.request.DrugAllergyCrossCreateRequest;
import com.smarthis.pharma.dto.request.DrugAllergyCrossQueryRequest;
import com.smarthis.pharma.dto.response.DrugAllergyCrossVo;
import com.smarthis.pharma.entity.DrugAllergyCross;
import com.smarthis.pharma.mapper.DrugAllergyCrossMapper;
import com.smarthis.pharma.service.DrugAllergyCrossService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugAllergyCrossServiceImpl implements DrugAllergyCrossService {

    private final DrugAllergyCrossMapper drugAllergyCrossMapper;

    @Override
    @Transactional
    public DrugAllergyCrossVo create(DrugAllergyCrossCreateRequest request) {
        DrugAllergyCross entity = DrugAllergyCrossConverter.toEntity(request);
        drugAllergyCrossMapper.insert(entity);
        log.info("DrugAllergyCross created: id={}, allergyCode={}", entity.getId(), entity.getAllergyCode());
        return DrugAllergyCrossConverter.toVo(entity);
    }

    @Override
    public PageResult<DrugAllergyCrossVo> query(DrugAllergyCrossQueryRequest request) {
        LambdaQueryWrapper<DrugAllergyCross> query = new LambdaQueryWrapper<>();
        query.eq(DrugAllergyCross::getDeleted, 0)
                .eq(DrugAllergyCross::getIsActive, 1);
        if (StringUtils.hasText(request.getAllergyCode())) {
            query.eq(DrugAllergyCross::getAllergyCode, request.getAllergyCode());
        }
        query.orderByDesc(DrugAllergyCross::getCreatedTime);

        Page<DrugAllergyCross> page = request.toPage();
        IPage<DrugAllergyCross> result = drugAllergyCrossMapper.selectPage(page, query);
        List<DrugAllergyCrossVo> vos = result.getRecords().stream()
                .map(DrugAllergyCrossConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public DrugAllergyCrossVo update(Long id, DrugAllergyCrossCreateRequest request) {
        DrugAllergyCross existing = drugAllergyCrossMapper.selectById(id);
        if (existing == null || existing.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        existing.setAllergyCode(request.getAllergyCode());
        existing.setAllergyName(request.getAllergyName());
        existing.setCrossDrugCode(request.getCrossDrugCode());
        existing.setCrossDrugName(request.getCrossDrugName());
        existing.setCrossLevel(request.getCrossLevel());
        existing.setDescription(request.getDescription());
        drugAllergyCrossMapper.updateById(existing);
        return DrugAllergyCrossConverter.toVo(existing);
    }
}
