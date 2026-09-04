package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.resource.converter.DrugPriceConverter;
import com.smarthis.resource.dto.request.DrugPriceCreateRequest;
import com.smarthis.resource.dto.response.DrugPriceVo;
import com.smarthis.resource.entity.DrugPrice;
import com.smarthis.resource.mapper.DrugPriceMapper;
import com.smarthis.resource.service.DrugPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugPriceServiceImpl implements DrugPriceService {

    private final DrugPriceMapper drugPriceMapper;

    @Override
    @Transactional
    public DrugPriceVo create(DrugPriceCreateRequest request) {
        DrugPrice entity = DrugPriceConverter.toEntity(request);
        drugPriceMapper.insert(entity);
        return DrugPriceConverter.toVo(entity);
    }

    @Override
    public List<DrugPriceVo> listByDrug(Long drugId) {
        LambdaQueryWrapper<DrugPrice> query = new LambdaQueryWrapper<>();
        query.eq(DrugPrice::getDrugId, drugId)
                .eq(DrugPrice::getIsActive, 1)
                .orderByDesc(DrugPrice::getEffectiveFrom);
        return drugPriceMapper.selectList(query).stream().map(DrugPriceConverter::toVo).toList();
    }

    @Override
    public List<DrugPriceVo> listByPharmacy(Long pharmacyId) {
        LambdaQueryWrapper<DrugPrice> query = new LambdaQueryWrapper<>();
        query.eq(DrugPrice::getPharmacyId, pharmacyId)
                .eq(DrugPrice::getIsActive, 1)
                .orderByDesc(DrugPrice::getEffectiveFrom);
        return drugPriceMapper.selectList(query).stream().map(DrugPriceConverter::toVo).toList();
    }
}
