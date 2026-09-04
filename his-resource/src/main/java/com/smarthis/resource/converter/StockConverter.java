package com.smarthis.resource.converter;

import com.smarthis.resource.dto.response.StockVo;
import com.smarthis.resource.entity.Stock;

public final class StockConverter {

    private StockConverter() {
    }

    public static StockVo toVo(Stock e) {
        StockVo vo = new StockVo();
        vo.setId(e.getId());
        vo.setDrugId(e.getDrugId());
        vo.setPharmacyId(e.getPharmacyId());
        vo.setBatchNo(e.getBatchNo());
        vo.setQuantity(e.getQuantity());
        vo.setUnitCost(e.getUnitCost());
        vo.setProduceDate(e.getProduceDate());
        vo.setExpiryDate(e.getExpiryDate());
        vo.setSupplierId(e.getSupplierId());
        vo.setWarehouseArea(e.getWarehouseArea());
        vo.setStockStatus(e.getStockStatus());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }
}
