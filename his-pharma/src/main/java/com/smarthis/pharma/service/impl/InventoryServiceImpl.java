package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.pharma.dto.request.InventoryOperationRequest;
import com.smarthis.pharma.dto.response.InventoryBatchVo;
import com.smarthis.pharma.dto.response.InventoryTransactionVo;
import com.smarthis.pharma.entity.DrugCatalog;
import com.smarthis.pharma.entity.InventoryBatch;
import com.smarthis.pharma.entity.InventoryTransaction;
import com.smarthis.pharma.mapper.DrugCatalogMapper;
import com.smarthis.pharma.mapper.InventoryBatchMapper;
import com.smarthis.pharma.mapper.InventoryTransactionMapper;
import com.smarthis.pharma.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private static final Set<String> OPERATIONS = Set.of("INBOUND", "OUTBOUND", "STOCKTAKE", "RETURN", "LOSS");
    private final InventoryBatchMapper batchMapper;
    private final InventoryTransactionMapper transactionMapper;
    private final DrugCatalogMapper drugCatalogMapper;

    @Override
    @Transactional
    public InventoryTransactionVo operate(InventoryOperationRequest request) {
        String type = request.getOperationType().trim().toUpperCase();
        if (!OPERATIONS.contains(type)) throw new BusinessException(ErrorCode.INVENTORY_OPERATION_INVALID);
        DrugCatalog drug = drugCatalogMapper.selectById(request.getDrugId());
        if (drug == null || Integer.valueOf(1).equals(drug.getDeleted())) throw new BusinessException(ErrorCode.DRUG_NOT_FOUND);
        if (!Integer.valueOf(1).equals(drug.getIsActive())) throw new BusinessException(ErrorCode.DRUG_STATUS_INVALID);
        if (request.getQuantity().signum() < 0 || (!"STOCKTAKE".equals(type) && request.getQuantity().signum() == 0)) {
            throw new BusinessException(ErrorCode.INVENTORY_OPERATION_INVALID);
        }

        InventoryBatch batch = "INBOUND".equals(type) ? findOrCreateInboundBatch(request) : requireBatch(request);
        BigDecimal before = batch.getAvailableQuantity();
        BigDecimal change;
        int affected;
        switch (type) {
            case "INBOUND", "RETURN" -> {
                validateNotExpired(batch);
                affected = batchMapper.addAvailable(batch.getId(), request.getQuantity());
                change = request.getQuantity();
            }
            case "OUTBOUND", "LOSS" -> {
                validateNotExpired(batch);
                affected = batchMapper.deductAvailable(batch.getId(), request.getQuantity());
                change = request.getQuantity().negate();
            }
            case "STOCKTAKE" -> {
                affected = batchMapper.setAvailable(batch.getId(), request.getQuantity(), batch.getVersion());
                change = request.getQuantity().subtract(before);
            }
            default -> throw new BusinessException(ErrorCode.INVENTORY_OPERATION_INVALID);
        }
        if (affected != 1) {
            if ("OUTBOUND".equals(type) || "LOSS".equals(type)) throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
            throw new BusinessException(ErrorCode.INVENTORY_OPERATION_INVALID);
        }
        InventoryBatch updated = batchMapper.selectById(batch.getId());
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNo(nextTransactionNo());
        transaction.setOperationType(type);
        transaction.setDrugId(request.getDrugId());
        transaction.setBatchId(batch.getId());
        transaction.setWarehouseCode(batch.getWarehouseCode());
        transaction.setQuantityChange(change);
        transaction.setQuantityBefore(before);
        transaction.setQuantityAfter(updated.getAvailableQuantity());
        transaction.setReferenceType(request.getReferenceType());
        transaction.setReferenceId(request.getReferenceId());
        transaction.setOperatorId(request.getOperatorId());
        transaction.setOperatorName(request.getOperatorName());
        transaction.setReason(request.getReason());
        transaction.setOccurredTime(LocalDateTime.now());
        transactionMapper.insert(transaction);
        return toTransactionVo(transaction);
    }

    @Override
    public List<InventoryBatchVo> listBatches(Long drugId, String warehouseCode, boolean availableOnly) {
        LambdaQueryWrapper<InventoryBatch> query = new LambdaQueryWrapper<>();
        query.eq(InventoryBatch::getDeleted, 0).eq(InventoryBatch::getIsActive, 1);
        query.eq(drugId != null, InventoryBatch::getDrugId, drugId);
        query.eq(StringUtils.hasText(warehouseCode), InventoryBatch::getWarehouseCode, warehouseCode);
        query.gt(availableOnly, InventoryBatch::getAvailableQuantity, BigDecimal.ZERO);
        query.orderByAsc(InventoryBatch::getExpiryDate).orderByAsc(InventoryBatch::getBatchNo);
        return batchMapper.selectList(query).stream().map(this::toBatchVo).toList();
    }

    @Override
    public List<InventoryBatchVo> listNearExpiry(String warehouseCode, int days) {
        int safeDays = Math.max(1, Math.min(days, 365));
        LambdaQueryWrapper<InventoryBatch> query = new LambdaQueryWrapper<>();
        query.eq(InventoryBatch::getDeleted, 0).eq(InventoryBatch::getIsActive, 1)
                .gt(InventoryBatch::getAvailableQuantity, BigDecimal.ZERO)
                .between(InventoryBatch::getExpiryDate, LocalDate.now(), LocalDate.now().plusDays(safeDays));
        query.eq(StringUtils.hasText(warehouseCode), InventoryBatch::getWarehouseCode, warehouseCode);
        query.orderByAsc(InventoryBatch::getExpiryDate);
        return batchMapper.selectList(query).stream().map(this::toBatchVo).toList();
    }

    @Override
    public List<InventoryTransactionVo> trace(Long drugId, Long batchId) {
        LambdaQueryWrapper<InventoryTransaction> query = new LambdaQueryWrapper<>();
        query.eq(InventoryTransaction::getDeleted, 0);
        query.eq(drugId != null, InventoryTransaction::getDrugId, drugId);
        query.eq(batchId != null, InventoryTransaction::getBatchId, batchId);
        query.orderByDesc(InventoryTransaction::getOccurredTime);
        return transactionMapper.selectList(query).stream().map(InventoryServiceImpl::toTransactionVo).toList();
    }

    private InventoryBatch findOrCreateInboundBatch(InventoryOperationRequest request) {
        if (!StringUtils.hasText(request.getBatchNo()) || request.getExpiryDate() == null || request.getUnitCost() == null) {
            throw new BusinessException(ErrorCode.INVENTORY_OPERATION_INVALID);
        }
        LambdaQueryWrapper<InventoryBatch> query = new LambdaQueryWrapper<>();
        query.eq(InventoryBatch::getDrugId, request.getDrugId())
                .eq(InventoryBatch::getWarehouseCode, request.getWarehouseCode())
                .eq(InventoryBatch::getBatchNo, request.getBatchNo()).eq(InventoryBatch::getDeleted, 0);
        InventoryBatch existing = batchMapper.selectOne(query);
        if (existing != null) return existing;
        InventoryBatch batch = new InventoryBatch();
        batch.setDrugId(request.getDrugId()); batch.setWarehouseCode(request.getWarehouseCode());
        batch.setBatchNo(request.getBatchNo()); batch.setProductionDate(request.getProductionDate());
        batch.setExpiryDate(request.getExpiryDate()); batch.setUnitCost(request.getUnitCost());
        batch.setQuantity(BigDecimal.ZERO); batch.setAvailableQuantity(BigDecimal.ZERO); batch.setLockedQuantity(BigDecimal.ZERO);
        batch.setIsActive(1); batch.setVersion(0);
        validateNotExpired(batch);
        batchMapper.insert(batch);
        return batch;
    }

    private InventoryBatch requireBatch(InventoryOperationRequest request) {
        if (request.getBatchId() == null) throw new BusinessException(ErrorCode.INVENTORY_OPERATION_INVALID);
        InventoryBatch batch = batchMapper.selectById(request.getBatchId());
        if (batch == null || Integer.valueOf(1).equals(batch.getDeleted()) || !batch.getDrugId().equals(request.getDrugId())
                || !batch.getWarehouseCode().equals(request.getWarehouseCode())) {
            throw new BusinessException(ErrorCode.INVENTORY_BATCH_NOT_FOUND);
        }
        return batch;
    }

    private static void validateNotExpired(InventoryBatch batch) {
        if (batch.getExpiryDate() == null || batch.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.BATCH_EXPIRED);
        }
    }

    private InventoryBatchVo toBatchVo(InventoryBatch e) {
        InventoryBatchVo vo = new InventoryBatchVo();
        vo.setId(e.getId()); vo.setDrugId(e.getDrugId()); vo.setWarehouseCode(e.getWarehouseCode()); vo.setBatchNo(e.getBatchNo());
        vo.setProductionDate(e.getProductionDate()); vo.setExpiryDate(e.getExpiryDate()); vo.setUnitCost(e.getUnitCost());
        vo.setQuantity(e.getQuantity()); vo.setAvailableQuantity(e.getAvailableQuantity()); vo.setLockedQuantity(e.getLockedQuantity());
        DrugCatalog drug = drugCatalogMapper.selectById(e.getDrugId());
        if (drug != null) { vo.setDrugCode(drug.getDrugCode()); vo.setDrugName(drug.getGenericName()); }
        LocalDate today = LocalDate.now();
        vo.setExpired(e.getExpiryDate() != null && e.getExpiryDate().isBefore(today));
        vo.setNearExpiry(!vo.isExpired() && e.getExpiryDate() != null && !e.getExpiryDate().isAfter(today.plusDays(90)));
        return vo;
    }

    private static InventoryTransactionVo toTransactionVo(InventoryTransaction e) {
        InventoryTransactionVo vo = new InventoryTransactionVo();
        vo.setId(e.getId()); vo.setTransactionNo(e.getTransactionNo()); vo.setOperationType(e.getOperationType());
        vo.setDrugId(e.getDrugId()); vo.setBatchId(e.getBatchId()); vo.setWarehouseCode(e.getWarehouseCode());
        vo.setQuantityChange(e.getQuantityChange()); vo.setQuantityBefore(e.getQuantityBefore()); vo.setQuantityAfter(e.getQuantityAfter());
        vo.setReferenceType(e.getReferenceType()); vo.setReferenceId(e.getReferenceId()); vo.setOperatorId(e.getOperatorId());
        vo.setOperatorName(e.getOperatorName()); vo.setReason(e.getReason()); vo.setOccurredTime(e.getOccurredTime());
        return vo;
    }

    private static String nextTransactionNo() {
        return "ST" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
