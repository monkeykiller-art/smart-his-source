package com.smarthis.pharma.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.pharma.dto.request.DispenseCreateRequest;
import com.smarthis.pharma.dto.request.DispenseItemRequest;
import com.smarthis.pharma.dto.request.InventoryOperationRequest;
import com.smarthis.pharma.dto.response.DispenseItemVo;
import com.smarthis.pharma.dto.response.DispenseVo;
import com.smarthis.pharma.entity.*;
import com.smarthis.pharma.mapper.*;
import com.smarthis.pharma.service.DispenseService;
import com.smarthis.pharma.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DispenseServiceImpl implements DispenseService {
    private final DispenseMapper dispenseMapper;
    private final DispenseItemMapper dispenseItemMapper;
    private final RxReviewMapper rxReviewMapper;
    private final InventoryBatchMapper batchMapper;
    private final DrugCatalogMapper drugCatalogMapper;
    private final InventoryService inventoryService;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public DispenseVo dispense(DispenseCreateRequest request) {
        RxReview review = rxReviewMapper.selectById(request.getRxReviewId());
        if (review == null || !"APPROVED".equals(review.getReviewStatus()) || !request.getPatientId().equals(review.getPatientId())) {
            throw new BusinessException(ErrorCode.RX_REVIEW_REJECTED);
        }
        LambdaQueryWrapper<Dispense> duplicate = new LambdaQueryWrapper<>();
        duplicate.eq(Dispense::getPrescriptionId, request.getPrescriptionId())
                .eq(Dispense::getDeleted, 0).ne(Dispense::getDispenseStatus, "RETURNED");
        if (dispenseMapper.selectCount(duplicate) > 0) throw new BusinessException(ErrorCode.DISPENSE_STATUS_INVALID);

        Dispense dispense = new Dispense();
        dispense.setDispenseNo(bizNoGenerator.next(BizNoType.DISPENSE));
        dispense.setPrescriptionId(request.getPrescriptionId()); dispense.setRxReviewId(request.getRxReviewId());
        dispense.setPatientId(request.getPatientId()); dispense.setWarehouseCode(request.getWarehouseCode());
        dispense.setDispenseStatus("DISPENSING"); dispense.setPharmacistId(request.getPharmacistId());
        dispense.setPharmacistName(request.getPharmacistName()); dispense.setRemark(request.getRemark());
        dispenseMapper.insert(dispense);

        for (DispenseItemRequest item : request.getItems()) allocate(dispense, item, request);
        dispense.setDispenseStatus("DISPENSED");
        dispense.setDispenseTime(LocalDateTime.now());
        dispenseMapper.updateById(dispense);
        return getById(dispense.getId());
    }

    private void allocate(Dispense dispense, DispenseItemRequest item, DispenseCreateRequest request) {
        DrugCatalog drug = drugCatalogMapper.selectById(item.getDrugId());
        if (drug == null || !Integer.valueOf(1).equals(drug.getIsActive())) throw new BusinessException(ErrorCode.DRUG_NOT_FOUND);
        LambdaQueryWrapper<InventoryBatch> query = new LambdaQueryWrapper<>();
        query.eq(InventoryBatch::getDrugId, item.getDrugId())
                .eq(InventoryBatch::getWarehouseCode, request.getWarehouseCode())
                .eq(InventoryBatch::getDeleted, 0).eq(InventoryBatch::getIsActive, 1)
                .ge(InventoryBatch::getExpiryDate, LocalDate.now())
                .gt(InventoryBatch::getAvailableQuantity, BigDecimal.ZERO)
                .orderByAsc(InventoryBatch::getExpiryDate).orderByAsc(InventoryBatch::getBatchNo);
        List<InventoryBatch> batches = batchMapper.selectList(query);
        BigDecimal remaining = item.getQuantity();
        for (InventoryBatch batch : batches) {
            if (remaining.signum() == 0) break;
            BigDecimal used = remaining.min(batch.getAvailableQuantity());
            InventoryOperationRequest operation = operation("OUTBOUND", dispense, batch, used,
                    request.getPharmacistId(), request.getPharmacistName(), "门诊发药");
            inventoryService.operate(operation);

            DispenseItem allocated = new DispenseItem();
            allocated.setDispenseId(dispense.getId()); allocated.setPrescriptionItemId(item.getPrescriptionItemId());
            allocated.setDrugId(item.getDrugId()); allocated.setBatchId(batch.getId()); allocated.setBatchNo(batch.getBatchNo());
            allocated.setExpiryDate(batch.getExpiryDate()); allocated.setQuantity(used); allocated.setUnit(item.getUnit());
            dispenseItemMapper.insert(allocated);
            remaining = remaining.subtract(used);
        }
        if (remaining.signum() > 0) throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
    }

    @Override
    public DispenseVo getById(Long id) {
        Dispense dispense = requireDispense(id);
        return toVo(dispense, listItems(id));
    }

    @Override
    public List<DispenseVo> listByPatient(Long patientId) {
        LambdaQueryWrapper<Dispense> query = new LambdaQueryWrapper<>();
        query.eq(Dispense::getPatientId, patientId).eq(Dispense::getDeleted, 0)
                .orderByDesc(Dispense::getDispenseTime);
        return dispenseMapper.selectList(query).stream().map(d -> toVo(d, listItems(d.getId()))).toList();
    }

    @Override
    @Transactional
    public DispenseVo returnAll(Long id, Long operatorId, String operatorName, String reason) {
        Dispense dispense = requireDispense(id);
        if (!"DISPENSED".equals(dispense.getDispenseStatus())) throw new BusinessException(ErrorCode.DISPENSE_STATUS_INVALID);
        for (DispenseItem item : listItems(id)) {
            InventoryBatch batch = batchMapper.selectById(item.getBatchId());
            inventoryService.operate(operation("RETURN", dispense, batch, item.getQuantity(), operatorId, operatorName, reason));
        }
        dispense.setDispenseStatus("RETURNED");
        dispense.setReturnTime(LocalDateTime.now());
        dispenseMapper.updateById(dispense);
        return getById(id);
    }

    private InventoryOperationRequest operation(String type, Dispense dispense, InventoryBatch batch,
                                                BigDecimal quantity, Long operatorId, String operatorName, String reason) {
        InventoryOperationRequest request = new InventoryOperationRequest();
        request.setOperationType(type); request.setDrugId(batch.getDrugId()); request.setBatchId(batch.getId());
        request.setWarehouseCode(batch.getWarehouseCode()); request.setQuantity(quantity);
        request.setReferenceType("DISPENSE"); request.setReferenceId(dispense.getId());
        request.setOperatorId(operatorId); request.setOperatorName(operatorName); request.setReason(reason);
        return request;
    }

    private Dispense requireDispense(Long id) {
        Dispense entity = dispenseMapper.selectById(id);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeleted())) throw new BusinessException(ErrorCode.DISPENSE_NOT_FOUND);
        return entity;
    }

    private List<DispenseItem> listItems(Long dispenseId) {
        LambdaQueryWrapper<DispenseItem> query = new LambdaQueryWrapper<>();
        query.eq(DispenseItem::getDispenseId, dispenseId).eq(DispenseItem::getDeleted, 0).orderByAsc(DispenseItem::getId);
        return dispenseItemMapper.selectList(query);
    }

    private DispenseVo toVo(Dispense e, List<DispenseItem> items) {
        DispenseVo vo = new DispenseVo();
        vo.setId(e.getId()); vo.setDispenseNo(e.getDispenseNo()); vo.setPrescriptionId(e.getPrescriptionId());
        vo.setRxReviewId(e.getRxReviewId()); vo.setPatientId(e.getPatientId()); vo.setWarehouseCode(e.getWarehouseCode());
        vo.setDispenseStatus(e.getDispenseStatus()); vo.setPharmacistId(e.getPharmacistId()); vo.setPharmacistName(e.getPharmacistName());
        vo.setDispenseTime(e.getDispenseTime()); vo.setReturnTime(e.getReturnTime()); vo.setRemark(e.getRemark());
        List<DispenseItemVo> itemVos = new ArrayList<>();
        for (DispenseItem item : items) {
            DispenseItemVo itemVo = new DispenseItemVo();
            itemVo.setId(item.getId()); itemVo.setPrescriptionItemId(item.getPrescriptionItemId()); itemVo.setDrugId(item.getDrugId());
            itemVo.setBatchId(item.getBatchId()); itemVo.setBatchNo(item.getBatchNo()); itemVo.setExpiryDate(item.getExpiryDate());
            itemVo.setQuantity(item.getQuantity()); itemVo.setUnit(item.getUnit());
            DrugCatalog drug = drugCatalogMapper.selectById(item.getDrugId());
            if (drug != null) { itemVo.setDrugCode(drug.getDrugCode()); itemVo.setDrugName(drug.getGenericName()); itemVo.setStrength(drug.getStrength()); }
            itemVos.add(itemVo);
        }
        vo.setItems(itemVos);
        return vo;
    }
}
