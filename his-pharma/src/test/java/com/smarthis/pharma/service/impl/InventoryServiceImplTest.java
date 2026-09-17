package com.smarthis.pharma.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.pharma.dto.request.InventoryOperationRequest;
import com.smarthis.pharma.dto.response.InventoryTransactionVo;
import com.smarthis.pharma.entity.DrugCatalog;
import com.smarthis.pharma.entity.InventoryBatch;
import com.smarthis.pharma.entity.InventoryTransaction;
import com.smarthis.pharma.mapper.DrugCatalogMapper;
import com.smarthis.pharma.mapper.InventoryBatchMapper;
import com.smarthis.pharma.mapper.InventoryTransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {
    @Mock private InventoryBatchMapper batchMapper;
    @Mock private InventoryTransactionMapper transactionMapper;
    @Mock private DrugCatalogMapper drugCatalogMapper;
    private InventoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InventoryServiceImpl(batchMapper, transactionMapper, drugCatalogMapper);
        DrugCatalog drug = new DrugCatalog();
        drug.setId(1L); drug.setIsActive(1); drug.setDeleted(0);
        when(drugCatalogMapper.selectById(1L)).thenReturn(drug);
    }

    @Test
    void outboundWritesAuditableTransaction() {
        InventoryBatch before = batch(new BigDecimal("10"));
        InventoryBatch after = batch(new BigDecimal("7"));
        when(batchMapper.selectById(2L)).thenReturn(before, after);
        when(batchMapper.deductAvailable(2L, new BigDecimal("3"))).thenReturn(1);
        doAnswer(invocation -> { ((InventoryTransaction) invocation.getArgument(0)).setId(99L); return 1; })
                .when(transactionMapper).insert(any());

        InventoryTransactionVo result = service.operate(request("OUTBOUND", new BigDecimal("3")));

        assertEquals(new BigDecimal("-3"), result.getQuantityChange());
        assertEquals(new BigDecimal("10"), result.getQuantityBefore());
        assertEquals(new BigDecimal("7"), result.getQuantityAfter());
        assertEquals(99L, result.getId());
    }

    @Test
    void concurrentOrInsufficientOutboundCannotCreateNegativeStock() {
        when(batchMapper.selectById(2L)).thenReturn(batch(new BigDecimal("2")));
        when(batchMapper.deductAvailable(2L, new BigDecimal("3"))).thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.operate(request("OUTBOUND", new BigDecimal("3"))));

        assertEquals(4002, error.getCode());
        verify(transactionMapper, never()).insert(any());
    }

    @Test
    void expiredBatchCannotLeaveWarehouse() {
        InventoryBatch batch = batch(new BigDecimal("10"));
        batch.setExpiryDate(LocalDate.now().minusDays(1));
        when(batchMapper.selectById(2L)).thenReturn(batch);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.operate(request("OUTBOUND", BigDecimal.ONE)));

        assertEquals(4003, error.getCode());
        verify(batchMapper, never()).deductAvailable(any(), any());
    }

    private static InventoryBatch batch(BigDecimal quantity) {
        InventoryBatch batch = new InventoryBatch();
        batch.setId(2L); batch.setDrugId(1L); batch.setWarehouseCode("OPD"); batch.setBatchNo("B001");
        batch.setExpiryDate(LocalDate.now().plusYears(1)); batch.setQuantity(quantity);
        batch.setAvailableQuantity(quantity); batch.setLockedQuantity(BigDecimal.ZERO);
        batch.setVersion(0); batch.setIsActive(1); batch.setDeleted(0);
        return batch;
    }

    private static InventoryOperationRequest request(String type, BigDecimal quantity) {
        InventoryOperationRequest request = new InventoryOperationRequest();
        request.setOperationType(type); request.setDrugId(1L); request.setBatchId(2L);
        request.setWarehouseCode("OPD"); request.setQuantity(quantity); request.setOperatorId(8L);
        return request;
    }
}
