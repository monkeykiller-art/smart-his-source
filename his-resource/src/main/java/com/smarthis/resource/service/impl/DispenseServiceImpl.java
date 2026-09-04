package com.smarthis.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.resource.converter.DispenseConverter;
import com.smarthis.resource.dto.request.DispenseCreateRequest;
import com.smarthis.resource.dto.request.DispenseQueryRequest;
import com.smarthis.resource.dto.response.DispenseVo;
import com.smarthis.resource.entity.Dispense;
import com.smarthis.resource.entity.DispenseItem;
import com.smarthis.resource.entity.Stock;
import com.smarthis.resource.enums.DispenseStatus;
import com.smarthis.resource.mapper.DispenseItemMapper;
import com.smarthis.resource.mapper.DispenseMapper;
import com.smarthis.resource.mapper.StockMapper;
import com.smarthis.resource.service.DispenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DispenseServiceImpl implements DispenseService {

    private final DispenseMapper dispenseMapper;
    private final DispenseItemMapper dispenseItemMapper;
    private final StockMapper stockMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public DispenseVo create(DispenseCreateRequest request) {
        Dispense dispense = DispenseConverter.toEntity(request);
        dispense.setDispenseNo(bizNoGenerator.next(BizNoType.DISPENSE));
        dispense.setDispenseTime(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (DispenseCreateRequest.DispenseItemRequest item : request.getItems()) {
                if (item.getUnitPrice() != null && item.getQuantity() != null) {
                    totalAmount = totalAmount.add(item.getUnitPrice().multiply(item.getQuantity()));
                }
            }
        }
        dispense.setTotalAmount(totalAmount);
        dispenseMapper.insert(dispense);

        if (request.getItems() != null) {
            for (DispenseCreateRequest.DispenseItemRequest itemReq : request.getItems()) {
                DispenseItem item = DispenseConverter.toItemEntity(dispense.getId(), itemReq);
                dispenseItemMapper.insert(item);
                deductStock(item.getDrugId(), request.getPharmacyId(), item.getQuantity());
            }
        }

        List<DispenseItem> items = listItemsByDispenseId(dispense.getId());
        return DispenseConverter.toVo(dispense, items);
    }

    @Override
    public DispenseVo getById(Long id) {
        Dispense dispense = requireDispense(id);
        List<DispenseItem> items = listItemsByDispenseId(id);
        return DispenseConverter.toVo(dispense, items);
    }

    @Override
    public PageResult<DispenseVo> list(DispenseQueryRequest request) {
        Page<Dispense> page = request.toPage();
        LambdaQueryWrapper<Dispense> query = new LambdaQueryWrapper<>();
        query.eq(Dispense::getDeleted, 0);
        if (request.getPatientId() != null) {
            query.eq(Dispense::getPatientId, request.getPatientId());
        }
        if (request.getPharmacyId() != null) {
            query.eq(Dispense::getPharmacyId, request.getPharmacyId());
        }
        if (StringUtils.hasText(request.getDispenseStatus())) {
            query.eq(Dispense::getDispenseStatus, request.getDispenseStatus());
        }
        if (StringUtils.hasText(request.getDispenseType())) {
            query.eq(Dispense::getDispenseType, request.getDispenseType());
        }
        query.orderByDesc(Dispense::getDispenseTime);
        Page<Dispense> result = dispenseMapper.selectPage(page, query);
        List<DispenseVo> records = new ArrayList<>();
        for (Dispense d : result.getRecords()) {
            List<DispenseItem> items = listItemsByDispenseId(d.getId());
            records.add(DispenseConverter.toVo(d, items));
        }
        return new PageResult<>(records, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public DispenseVo review(Long id, String reviewerId) {
        Dispense dispense = requireDispense(id);
        if (!DispenseStatus.PENDING.name().equals(dispense.getDispenseStatus())) {
            throw new BusinessException(ErrorCode.DISPENSE_STATUS_INVALID);
        }
        dispense.setReviewerId(Long.valueOf(reviewerId));
        dispense.setReviewTime(LocalDateTime.now());
        dispense.setDispenseStatus("REVIEWED");
        dispenseMapper.updateById(dispense);
        List<DispenseItem> items = listItemsByDispenseId(id);
        return DispenseConverter.toVo(dispense, items);
    }

    private Dispense requireDispense(Long id) {
        Dispense dispense = dispenseMapper.selectById(id);
        if (dispense == null || dispense.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.DISPENSE_NOT_FOUND);
        }
        return dispense;
    }

    private List<DispenseItem> listItemsByDispenseId(Long dispenseId) {
        LambdaQueryWrapper<DispenseItem> query = new LambdaQueryWrapper<>();
        query.eq(DispenseItem::getDispenseId, dispenseId).eq(DispenseItem::getDeleted, 0);
        return dispenseItemMapper.selectList(query);
    }

    private void deductStock(Long drugId, Long pharmacyId, BigDecimal quantity) {
        LambdaQueryWrapper<Stock> query = new LambdaQueryWrapper<>();
        query.eq(Stock::getDrugId, drugId)
                .eq(Stock::getPharmacyId, pharmacyId)
                .eq(Stock::getDeleted, 0)
                .ge(Stock::getQuantity, quantity)
                .orderByAsc(Stock::getExpiryDate)
                .last("LIMIT 1");
        Stock stock = stockMapper.selectOne(query);
        if (stock == null) {
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
        }
        stock.setQuantity(stock.getQuantity().subtract(quantity));
        stockMapper.updateById(stock);
    }
}
