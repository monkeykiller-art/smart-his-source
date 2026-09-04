package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.converter.DrugConverter;
import com.smarthis.resource.dto.request.DrugCreateRequest;
import com.smarthis.resource.dto.request.DrugQueryRequest;
import com.smarthis.resource.dto.request.DrugUpdateRequest;
import com.smarthis.resource.dto.response.DrugVo;
import com.smarthis.resource.entity.Drug;
import com.smarthis.resource.mapper.DrugMapper;
import com.smarthis.resource.service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugServiceImpl implements DrugService {

    private final DrugMapper drugMapper;

    @Override
    @Transactional
    public DrugVo create(DrugCreateRequest request) {
        Drug drug = DrugConverter.toEntity(request);
        drugMapper.insert(drug);
        return DrugConverter.toVo(drug);
    }

    @Override
    @Transactional
    public DrugVo update(Long id, DrugUpdateRequest request) {
        Drug drug = requireDrug(id);
        DrugConverter.applyUpdate(request, drug);
        drugMapper.updateById(drug);
        return DrugConverter.toVo(drug);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Drug drug = requireDrug(id);
        drug.setDeleted(1);
        drugMapper.updateById(drug);
    }

    @Override
    public DrugVo getById(Long id) {
        return DrugConverter.toVo(requireDrug(id));
    }

    @Override
    public PageResult<DrugVo> list(DrugQueryRequest request) {
        Page<Drug> page = request.toPage();
        LambdaQueryWrapper<Drug> query = new LambdaQueryWrapper<>();
        query.eq(Drug::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            query.and(w -> w.like(Drug::getDrugName, request.getKeyword())
                    .or().like(Drug::getNamePinyin, request.getKeyword())
                    .or().like(Drug::getGenericName, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getDrugType())) {
            query.eq(Drug::getDrugType, request.getDrugType());
        }
        if (request.getIsInsurance() != null) {
            query.eq(Drug::getIsInsurance, request.getIsInsurance());
        }
        if (request.getIsNarcotic() != null) {
            query.eq(Drug::getIsNarcotic, request.getIsNarcotic());
        }
        if (request.getIsAntibiotic() != null) {
            query.eq(Drug::getIsAntibiotic, request.getIsAntibiotic());
        }
        if (StringUtils.hasText(request.getDrugStatus())) {
            query.eq(Drug::getDrugStatus, request.getDrugStatus());
        }
        query.orderByAsc(Drug::getSortOrder);
        Page<Drug> result = drugMapper.selectPage(page, query);
        List<DrugVo> records = result.getRecords().stream().map(DrugConverter::toVo).toList();
        return new PageResult<>(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public List<DrugVo> search(String keyword) {
        LambdaQueryWrapper<Drug> query = new LambdaQueryWrapper<>();
        query.eq(Drug::getDeleted, 0).eq(Drug::getDrugStatus, "ACTIVE");
        if (StringUtils.hasText(keyword)) {
            query.and(w -> w.like(Drug::getDrugName, keyword)
                    .or().like(Drug::getNamePinyin, keyword)
                    .or().like(Drug::getGenericName, keyword));
        }
        query.orderByAsc(Drug::getSortOrder).last("LIMIT 50");
        return drugMapper.selectList(query).stream().map(DrugConverter::toVo).toList();
    }

    private Drug requireDrug(Long id) {
        Drug drug = drugMapper.selectById(id);
        if (drug == null || drug.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.DRUG_NOT_FOUND);
        }
        return drug;
    }
}
