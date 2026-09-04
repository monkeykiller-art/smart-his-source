package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.pharma.converter.DrugContraindicationConverter;
import com.smarthis.pharma.dto.request.DrugContraindicationCreateRequest;
import com.smarthis.pharma.dto.request.DrugContraindicationQueryRequest;
import com.smarthis.pharma.dto.response.DrugContraindicationVo;
import com.smarthis.pharma.entity.DrugContraindication;
import com.smarthis.pharma.mapper.DrugContraindicationMapper;
import com.smarthis.pharma.service.DrugContraindicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugContraindicationServiceImpl implements DrugContraindicationService {

    private final DrugContraindicationMapper drugContraindicationMapper;

    @Override
    @Transactional
    public DrugContraindicationVo create(DrugContraindicationCreateRequest request) {
        DrugContraindication entity = DrugContraindicationConverter.toEntity(request);
        drugContraindicationMapper.insert(entity);
        log.info("DrugContraindication created: id={}, drugCode={}, type={}", entity.getId(), entity.getDrugCode(), entity.getContraindicationType());
        return DrugContraindicationConverter.toVo(entity);
    }

    @Override
    public PageResult<DrugContraindicationVo> query(DrugContraindicationQueryRequest request) {
        LambdaQueryWrapper<DrugContraindication> query = new LambdaQueryWrapper<>();
        query.eq(DrugContraindication::getDeleted, 0)
                .eq(DrugContraindication::getIsActive, 1);
        if (StringUtils.hasText(request.getDrugCode())) {
            query.eq(DrugContraindication::getDrugCode, request.getDrugCode());
        }
        if (StringUtils.hasText(request.getContraindicationType())) {
            query.eq(DrugContraindication::getContraindicationType, request.getContraindicationType());
        }
        query.orderByDesc(DrugContraindication::getCreatedTime);

        Page<DrugContraindication> page = request.toPage();
        IPage<DrugContraindication> result = drugContraindicationMapper.selectPage(page, query);
        List<DrugContraindicationVo> vos = result.getRecords().stream()
                .map(DrugContraindicationConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public DrugContraindicationVo update(Long id, DrugContraindicationCreateRequest request) {
        DrugContraindication existing = drugContraindicationMapper.selectById(id);
        if (existing == null || existing.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        existing.setDrugCode(request.getDrugCode());
        existing.setContraindicationType(request.getContraindicationType());
        existing.setContraindicationCode(request.getContraindicationCode());
        existing.setContraindicationName(request.getContraindicationName());
        existing.setSeverityLevel(request.getSeverityLevel());
        existing.setDescription(request.getDescription());
        existing.setSuggestion(request.getSuggestion());
        drugContraindicationMapper.updateById(existing);
        return DrugContraindicationConverter.toVo(existing);
    }
}
