package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.converter.PharmacyConverter;
import com.smarthis.resource.dto.request.PharmacyCreateRequest;
import com.smarthis.resource.dto.request.PharmacyQueryRequest;
import com.smarthis.resource.dto.request.PharmacyUpdateRequest;
import com.smarthis.resource.dto.response.PharmacyVo;
import com.smarthis.resource.entity.Pharmacy;
import com.smarthis.resource.mapper.PharmacyMapper;
import com.smarthis.resource.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PharmacyServiceImpl implements PharmacyService {

    private final PharmacyMapper pharmacyMapper;

    @Override
    @Transactional
    public PharmacyVo create(PharmacyCreateRequest request) {
        Pharmacy entity = PharmacyConverter.toEntity(request);
        pharmacyMapper.insert(entity);
        return PharmacyConverter.toVo(entity);
    }

    @Override
    @Transactional
    public PharmacyVo update(Long id, PharmacyUpdateRequest request) {
        Pharmacy entity = requirePharmacy(id);
        PharmacyConverter.applyUpdate(request, entity);
        pharmacyMapper.updateById(entity);
        return PharmacyConverter.toVo(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Pharmacy entity = requirePharmacy(id);
        entity.setDeleted(1);
        pharmacyMapper.updateById(entity);
    }

    @Override
    public PharmacyVo getById(Long id) {
        return PharmacyConverter.toVo(requirePharmacy(id));
    }

    @Override
    public PageResult<PharmacyVo> list(PharmacyQueryRequest request) {
        Page<Pharmacy> page = request.toPage();
        LambdaQueryWrapper<Pharmacy> query = new LambdaQueryWrapper<>();
        query.eq(Pharmacy::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            query.and(w -> w.like(Pharmacy::getPharmacyName, request.getKeyword())
                    .or().like(Pharmacy::getPharmacyCode, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getPharmacyType())) {
            query.eq(Pharmacy::getPharmacyType, request.getPharmacyType());
        }
        if (StringUtils.hasText(request.getPharmacyStatus())) {
            query.eq(Pharmacy::getPharmacyStatus, request.getPharmacyStatus());
        }
        query.orderByAsc(Pharmacy::getPharmacyCode);
        Page<Pharmacy> result = pharmacyMapper.selectPage(page, query);
        List<PharmacyVo> records = result.getRecords().stream().map(PharmacyConverter::toVo).toList();
        return new PageResult<>(records, result.getTotal(), request.getPage(), request.getSize());
    }

    private Pharmacy requirePharmacy(Long id) {
        Pharmacy entity = pharmacyMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.STORAGE_NOT_FOUND);
        }
        return entity;
    }
}
