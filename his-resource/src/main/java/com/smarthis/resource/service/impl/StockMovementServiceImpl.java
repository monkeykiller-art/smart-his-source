package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.resource.converter.StockMovementConverter;
import com.smarthis.resource.dto.request.StockMovementCreateRequest;
import com.smarthis.resource.dto.response.StockMovementVo;
import com.smarthis.resource.entity.Stock;
import com.smarthis.resource.entity.StockMovement;
import com.smarthis.resource.mapper.StockMapper;
import com.smarthis.resource.mapper.StockMovementMapper;
import com.smarthis.resource.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementMapper stockMovementMapper;
    private final StockMapper stockMapper;

    @Override
    @Transactional
    public StockMovementVo create(StockMovementCreateRequest request) {
        Stock stock = findOrCreateStock(request);
        BigDecimal beforeQty = stock.getQuantity();
        boolean isIn = isIncoming(request.getMovementType());
        BigDecimal afterQty;
        if (isIn) {
            afterQty = beforeQty.add(request.getQuantity());
        } else {
            if (beforeQty.compareTo(request.getQuantity()) < 0) {
                throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
            }
            afterQty = beforeQty.subtract(request.getQuantity());
        }

        StockMovement movement = StockMovementConverter.toEntity(request);
        movement.setMovementNo(generateMovementNo());
        movement.setBeforeQty(beforeQty);
        movement.setAfterQty(afterQty);
        if (request.getUnitCost() != null) {
            movement.setTotalAmount(request.getUnitCost().multiply(request.getQuantity()));
        }
        if (movement.getMovementTime() == null) {
            movement.setMovementTime(LocalDateTime.now());
        }
        stockMovementMapper.insert(movement);

        stock.setQuantity(afterQty);
        stockMapper.updateById(stock);

        return StockMovementConverter.toVo(movement);
    }

    @Override
    public List<StockMovementVo> listByDrug(Long drugId, Long pharmacyId) {
        LambdaQueryWrapper<StockMovement> query = new LambdaQueryWrapper<>();
        query.eq(StockMovement::getDeleted, 0).eq(StockMovement::getDrugId, drugId);
        if (pharmacyId != null) {
            query.eq(StockMovement::getPharmacyId, pharmacyId);
        }
        query.orderByDesc(StockMovement::getMovementTime);
        return stockMovementMapper.selectList(query).stream().map(StockMovementConverter::toVo).toList();
    }

    private Stock findOrCreateStock(StockMovementCreateRequest request) {
        LambdaQueryWrapper<Stock> query = new LambdaQueryWrapper<>();
        query.eq(Stock::getDrugId, request.getDrugId())
                .eq(Stock::getPharmacyId, request.getPharmacyId())
                .eq(Stock::getDeleted, 0);
        if (request.getBatchNo() != null) {
            query.eq(Stock::getBatchNo, request.getBatchNo());
        }
        query.last("LIMIT 1");
        Stock stock = stockMapper.selectOne(query);
        if (stock == null) {
            stock = new Stock();
            stock.setDrugId(request.getDrugId());
            stock.setPharmacyId(request.getPharmacyId());
            stock.setBatchNo(request.getBatchNo());
            stock.setQuantity(BigDecimal.ZERO);
            stock.setUnitCost(request.getUnitCost() != null ? request.getUnitCost() : BigDecimal.ZERO);
            stock.setStockStatus("NORMAL");
            stockMapper.insert(stock);
        }
        return stock;
    }

    private boolean isIncoming(String movementType) {
        return movementType != null && movementType.startsWith("IN");
    }

    private String generateMovementNo() {
        return "CK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
