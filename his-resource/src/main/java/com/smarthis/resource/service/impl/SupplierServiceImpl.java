package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.converter.SupplierConverter;
import com.smarthis.resource.dto.request.SupplierCreateRequest;
import com.smarthis.resource.dto.request.SupplierQueryRequest;
import com.smarthis.resource.dto.request.SupplierUpdateRequest;
import com.smarthis.resource.dto.response.SupplierVo;
import com.smarthis.resource.entity.Supplier;
import com.smarthis.resource.mapper.SupplierMapper;
import com.smarthis.resource.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public SupplierVo create(SupplierCreateRequest request) {
        Supplier entity = SupplierConverter.toEntity(request);
        supplierMapper.insert(entity);
        return SupplierConverter.toVo(entity);
    }

    @Override
    @Transactional
    public SupplierVo update(Long id, SupplierUpdateRequest request) {
        Supplier entity = requireSupplier(id);
        SupplierConverter.applyUpdate(request, entity);
        supplierMapper.updateById(entity);
        return SupplierConverter.toVo(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Supplier entity = requireSupplier(id);
        entity.setDeleted(1);
        supplierMapper.updateById(entity);
    }

    @Override
    public SupplierVo getById(Long id) {
        return SupplierConverter.toVo(requireSupplier(id));
    }

    @Override
    public PageResult<SupplierVo> list(SupplierQueryRequest request) {
        Page<Supplier> page = request.toPage();
        LambdaQueryWrapper<Supplier> query = new LambdaQueryWrapper<>();
        query.eq(Supplier::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            query.and(w -> w.like(Supplier::getSupplierName, request.getKeyword())
                    .or().like(Supplier::getSupplierCode, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getSupplierType())) {
            query.eq(Supplier::getSupplierType, request.getSupplierType());
        }
        if (StringUtils.hasText(request.getSupplierStatus())) {
            query.eq(Supplier::getSupplierStatus, request.getSupplierStatus());
        }
        query.orderByAsc(Supplier::getSupplierCode);
        Page<Supplier> result = supplierMapper.selectPage(page, query);
        List<SupplierVo> records = result.getRecords().stream().map(SupplierConverter::toVo).toList();
        return new PageResult<>(records, result.getTotal(), request.getPage(), request.getSize());
    }

    private Supplier requireSupplier(Long id) {
        Supplier entity = supplierMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.SUPPLIER_NOT_FOUND);
        }
        return entity;
    }
}
