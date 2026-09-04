package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.model.PageResult;
import com.smarthis.resource.converter.StockConverter;
import com.smarthis.resource.dto.request.StockQueryRequest;
import com.smarthis.resource.dto.response.StockVo;
import com.smarthis.resource.entity.Drug;
import com.smarthis.resource.entity.Stock;
import com.smarthis.resource.mapper.DrugMapper;
import com.smarthis.resource.mapper.StockMapper;
import com.smarthis.resource.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockMapper stockMapper;
    private final DrugMapper drugMapper;

    @Override
    public PageResult<StockVo> list(StockQueryRequest request) {
        Page<Stock> page = request.toPage();
        LambdaQueryWrapper<Stock> query = new LambdaQueryWrapper<>();
        query.eq(Stock::getDeleted, 0);
        if (request.getDrugId() != null) {
            query.eq(Stock::getDrugId, request.getDrugId());
        }
        if (request.getPharmacyId() != null) {
            query.eq(Stock::getPharmacyId, request.getPharmacyId());
        }
        if (StringUtils.hasText(request.getStockStatus())) {
            query.eq(Stock::getStockStatus, request.getStockStatus());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            LambdaQueryWrapper<Drug> drugQuery = new LambdaQueryWrapper<>();
            drugQuery.eq(Drug::getDeleted, 0)
                    .like(Drug::getDrugName, request.getKeyword());
            List<Drug> drugs = drugMapper.selectList(drugQuery);
            Set<Long> drugIds = drugs.stream().map(Drug::getId).collect(Collectors.toSet());
            if (drugIds.isEmpty()) {
                return new PageResult<>(List.of(), 0, request.getPage(), request.getSize());
            }
            query.in(Stock::getDrugId, drugIds);
        }
        query.orderByDesc(Stock::getCreatedTime);
        Page<Stock> result = stockMapper.selectPage(page, query);
        List<StockVo> records = result.getRecords().stream().map(StockConverter::toVo).toList();
        return new PageResult<>(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    public List<StockVo> listByDrug(Long drugId, Long pharmacyId) {
        LambdaQueryWrapper<Stock> query = new LambdaQueryWrapper<>();
        query.eq(Stock::getDeleted, 0).eq(Stock::getDrugId, drugId);
        if (pharmacyId != null) {
            query.eq(Stock::getPharmacyId, pharmacyId);
        }
        query.orderByAsc(Stock::getExpiryDate);
        return stockMapper.selectList(query).stream().map(StockConverter::toVo).toList();
    }
}
