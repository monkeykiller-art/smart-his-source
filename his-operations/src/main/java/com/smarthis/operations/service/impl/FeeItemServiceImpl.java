package com.smarthis.operations.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.operations.converter.FeeItemConverter;
import com.smarthis.operations.dto.request.FeeItemCreateRequest;
import com.smarthis.operations.dto.request.FeeItemQueryRequest;
import com.smarthis.operations.dto.request.FeeItemUpdateRequest;
import com.smarthis.operations.dto.response.FeeItemVo;
import com.smarthis.operations.entity.FeeItem;
import com.smarthis.operations.mapper.FeeItemMapper;
import com.smarthis.operations.service.FeeItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeItemServiceImpl implements FeeItemService {

    private final FeeItemMapper feeItemMapper;

    @Override
    public FeeItemVo create(FeeItemCreateRequest request) {
        FeeItem entity = FeeItemConverter.toEntity(request);
        feeItemMapper.insert(entity);
        log.info("FeeItem created: id={}, code={}", entity.getId(), entity.getItemCode());
        return FeeItemConverter.toVo(entity);
    }

    @Override
    public FeeItemVo getById(Long id) {
        FeeItem entity = feeItemMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.CHARGE_ITEM_NOT_FOUND);
        }
        return FeeItemConverter.toVo(entity);
    }

    @Override
    public PageResult<FeeItemVo> query(FeeItemQueryRequest request) {
        LambdaQueryWrapper<FeeItem> query = new LambdaQueryWrapper<>();
        query.eq(FeeItem::getDeleted, 0);
        if (StringUtils.hasText(request.getKeyword())) {
            query.and(w -> w.like(FeeItem::getItemName, request.getKeyword())
                    .or().like(FeeItem::getItemCode, request.getKeyword())
                    .or().like(FeeItem::getNamePinyin, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getItemClass())) {
            query.eq(FeeItem::getItemClass, request.getItemClass());
        }
        query.orderByAsc(FeeItem::getSortOrder);

        Page<FeeItem> page = request.toPage();
        IPage<FeeItem> result = feeItemMapper.selectPage(page, query);
        List<FeeItemVo> vos = result.getRecords().stream()
                .map(FeeItemConverter::toVo)
                .toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public FeeItemVo update(Long id, FeeItemUpdateRequest request) {
        FeeItem entity = feeItemMapper.selectById(id);
        if (entity == null || entity.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.CHARGE_ITEM_NOT_FOUND);
        }
        if (StringUtils.hasText(request.getItemName())) {
            entity.setItemName(request.getItemName());
        }
        if (StringUtils.hasText(request.getNamePinyin())) {
            entity.setNamePinyin(request.getNamePinyin());
        }
        if (StringUtils.hasText(request.getItemClass())) {
            entity.setItemClass(request.getItemClass());
        }
        if (StringUtils.hasText(request.getItemCategory())) {
            entity.setItemCategory(request.getItemCategory());
        }
        if (StringUtils.hasText(request.getSpec())) {
            entity.setSpec(request.getSpec());
        }
        if (StringUtils.hasText(request.getUnit())) {
            entity.setUnit(request.getUnit());
        }
        if (request.getUnitPrice() != null) {
            entity.setUnitPrice(request.getUnitPrice());
        }
        if (StringUtils.hasText(request.getDosageForm())) {
            entity.setDosageForm(request.getDosageForm());
        }
        if (request.getIsInsurance() != null) {
            entity.setIsInsurance(request.getIsInsurance());
        }
        if (request.getInsuranceRatio() != null) {
            entity.setInsuranceRatio(request.getInsuranceRatio());
        }
        if (request.getIsSelfPay() != null) {
            entity.setIsSelfPay(request.getIsSelfPay());
        }
        if (StringUtils.hasText(request.getExecuteDeptType())) {
            entity.setExecuteDeptType(request.getExecuteDeptType());
        }
        if (request.getNeedConfirm() != null) {
            entity.setNeedConfirm(request.getNeedConfirm());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getItemStatus() != null) {
            entity.setItemStatus(request.getItemStatus());
        }
        feeItemMapper.updateById(entity);
        log.info("FeeItem updated: id={}", entity.getId());
        return FeeItemConverter.toVo(entity);
    }
}
