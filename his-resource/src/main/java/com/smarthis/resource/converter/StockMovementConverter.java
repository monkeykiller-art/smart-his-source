package com.smarthis.resource.converter;

import com.smarthis.resource.dto.request.StockMovementCreateRequest;
import com.smarthis.resource.dto.response.StockMovementVo;
import com.smarthis.resource.entity.StockMovement;

import java.time.LocalDateTime;

public final class StockMovementConverter {

    private StockMovementConverter() {
    }

    public static StockMovement toEntity(StockMovementCreateRequest req) {
        StockMovement e = new StockMovement();
        e.setDrugId(req.getDrugId());
        e.setPharmacyId(req.getPharmacyId());
        e.setBatchNo(req.getBatchNo());
        e.setMovementType(req.getMovementType());
        e.setQuantity(req.getQuantity());
        e.setUnitCost(req.getUnitCost());
        e.setReferenceType(req.getReferenceType());
        e.setReferenceId(req.getReferenceId());
        e.setOperatorId(req.getOperatorId());
        e.setMovementTime(req.getMovementTime() != null ? req.getMovementTime() : LocalDateTime.now());
        e.setRemark(req.getRemark());
        return e;
    }

    public static StockMovementVo toVo(StockMovement e) {
        StockMovementVo vo = new StockMovementVo();
        vo.setId(e.getId());
        vo.setMovementNo(e.getMovementNo());
        vo.setDrugId(e.getDrugId());
        vo.setPharmacyId(e.getPharmacyId());
        vo.setBatchNo(e.getBatchNo());
        vo.setMovementType(e.getMovementType());
        vo.setQuantity(e.getQuantity());
        vo.setUnitCost(e.getUnitCost());
        vo.setTotalAmount(e.getTotalAmount());
        vo.setBeforeQty(e.getBeforeQty());
        vo.setAfterQty(e.getAfterQty());
        vo.setReferenceType(e.getReferenceType());
        vo.setReferenceId(e.getReferenceId());
        vo.setOperatorId(e.getOperatorId());
        vo.setMovementTime(e.getMovementTime());
        vo.setRemark(e.getRemark());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }
}
