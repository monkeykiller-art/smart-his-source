package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.converter.DrugInteractionConverter;
import com.smarthis.pharma.dto.request.DrugInteractionCheckRequest;
import com.smarthis.pharma.dto.request.DrugInteractionCreateRequest;
import com.smarthis.pharma.dto.request.DrugInteractionQueryRequest;
import com.smarthis.pharma.dto.response.DrugInteractionVo;
import com.smarthis.pharma.entity.DrugInteraction;
import com.smarthis.pharma.mapper.DrugInteractionMapper;
import com.smarthis.pharma.service.DrugInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugInteractionServiceImpl implements DrugInteractionService {

    private final DrugInteractionMapper drugInteractionMapper;

    @Override
    @Transactional
    public DrugInteractionVo create(DrugInteractionCreateRequest request) {
        DrugInteraction entity = DrugInteractionConverter.toEntity(request);
        drugInteractionMapper.insert(entity);
        log.info("DrugInteraction created: id={}, drugA={}, drugB={}", entity.getId(), entity.getDrugCodeA(), entity.getDrugCodeB());
        return DrugInteractionConverter.toVo(entity);
    }

    @Override
    public PageResult<DrugInteractionVo> query(DrugInteractionQueryRequest request) {
        LambdaQueryWrapper<DrugInteraction> query = new LambdaQueryWrapper<>();
        query.eq(DrugInteraction::getDeleted, 0)
                .eq(DrugInteraction::getIsActive, 1);
        if (StringUtils.hasText(request.getDrugCodeA())) {
            query.eq(DrugInteraction::getDrugCodeA, request.getDrugCodeA());
        }
        if (StringUtils.hasText(request.getDrugCodeB())) {
            query.eq(DrugInteraction::getDrugCodeB, request.getDrugCodeB());
        }
        query.orderByDesc(DrugInteraction::getCreatedTime);

        Page<DrugInteraction> page = request.toPage();
        IPage<DrugInteraction> result = drugInteractionMapper.selectPage(page, query);
        List<DrugInteractionVo> vos = result.getRecords().stream()
                .map(DrugInteractionConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public DrugInteractionVo update(Long id, DrugInteractionCreateRequest request) {
        DrugInteraction existing = drugInteractionMapper.selectById(id);
        if (existing == null || existing.getDeleted() != 0) {
            throw new com.smarthis.common.exception.BusinessException(com.smarthis.common.model.ErrorCode.NOT_FOUND);
        }
        existing.setDrugCodeA(request.getDrugCodeA());
        existing.setDrugCodeB(request.getDrugCodeB());
        existing.setInteractionLevel(request.getInteractionLevel());
        existing.setInteractionDesc(request.getInteractionDesc());
        existing.setSuggestion(request.getSuggestion());
        existing.setReference(request.getReference());
        drugInteractionMapper.updateById(existing);
        return DrugInteractionConverter.toVo(existing);
    }

    @Override
    public List<DrugInteractionVo> check(DrugInteractionCheckRequest request) {
        LambdaQueryWrapper<DrugInteraction> query = new LambdaQueryWrapper<>();
        query.eq(DrugInteraction::getDeleted, 0)
                .eq(DrugInteraction::getIsActive, 1)
                .and(w -> w
                        .and(inner -> inner.eq(DrugInteraction::getDrugCodeA, request.getDrugCodeA())
                                .eq(DrugInteraction::getDrugCodeB, request.getDrugCodeB()))
                        .or(inner -> inner.eq(DrugInteraction::getDrugCodeA, request.getDrugCodeB())
                                .eq(DrugInteraction::getDrugCodeB, request.getDrugCodeA()))
                );
        return drugInteractionMapper.selectList(query).stream()
                .map(DrugInteractionConverter::toVo)
                .toList();
    }
}
