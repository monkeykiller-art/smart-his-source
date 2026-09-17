package com.smarthis.pharma.service;

import com.smarthis.pharma.dto.request.InventoryOperationRequest;
import com.smarthis.pharma.dto.response.InventoryBatchVo;
import com.smarthis.pharma.dto.response.InventoryTransactionVo;

import java.util.List;

public interface InventoryService {
    InventoryTransactionVo operate(InventoryOperationRequest request);
    List<InventoryBatchVo> listBatches(Long drugId, String warehouseCode, boolean availableOnly);
    List<InventoryBatchVo> listNearExpiry(String warehouseCode, int days);
    List<InventoryTransactionVo> trace(Long drugId, Long batchId);
}
