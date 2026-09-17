package com.smarthis.pharma.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.pharma.dto.request.DispenseCreateRequest;
import com.smarthis.pharma.dto.request.DispenseItemRequest;
import com.smarthis.pharma.entity.*;
import com.smarthis.pharma.mapper.*;
import com.smarthis.pharma.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispenseServiceImplTest {
    @Mock private DispenseMapper dispenseMapper;
    @Mock private DispenseItemMapper dispenseItemMapper;
    @Mock private RxReviewMapper rxReviewMapper;
    @Mock private InventoryBatchMapper batchMapper;
    @Mock private DrugCatalogMapper drugCatalogMapper;
    @Mock private InventoryService inventoryService;
    @Mock private BizNoGenerator bizNoGenerator;
    private DispenseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DispenseServiceImpl(dispenseMapper, dispenseItemMapper, rxReviewMapper, batchMapper,
                drugCatalogMapper, inventoryService, bizNoGenerator);
    }

    @Test
    void rejectsPrescriptionWithoutApprovedReview() {
        RxReview review = new RxReview();
        review.setPatientId(5L); review.setReviewStatus("REJECTED");
        when(rxReviewMapper.selectById(7L)).thenReturn(review);

        BusinessException error = assertThrows(BusinessException.class, () -> service.dispense(request()));

        assertEquals(6001, error.getCode());
        verifyNoInteractions(inventoryService);
    }

    @Test
    void allocatesNearestExpiryBatchFirst() {
        RxReview review = new RxReview();
        review.setPatientId(5L); review.setReviewStatus("APPROVED");
        when(rxReviewMapper.selectById(7L)).thenReturn(review);
        when(dispenseMapper.selectCount(any())).thenReturn(0L);
        when(bizNoGenerator.next(BizNoType.DISPENSE)).thenReturn("DP001");
        AtomicReference<Dispense> saved = new AtomicReference<>();
        doAnswer(inv -> { Dispense value = inv.getArgument(0); value.setId(20L); saved.set(value); return 1; }).when(dispenseMapper).insert(any());
        when(dispenseMapper.selectById(20L)).thenAnswer(inv -> saved.get());
        when(dispenseItemMapper.selectList(any())).thenReturn(List.of());
        DrugCatalog drug = new DrugCatalog(); drug.setId(1L); drug.setIsActive(1);
        when(drugCatalogMapper.selectById(1L)).thenReturn(drug);
        InventoryBatch early = batch(31L, LocalDate.now().plusMonths(1), new BigDecimal("2"));
        InventoryBatch later = batch(32L, LocalDate.now().plusMonths(6), new BigDecimal("5"));
        when(batchMapper.selectList(any())).thenReturn(List.of(early, later));

        assertEquals("DISPENSED", service.dispense(request()).getDispenseStatus());

        verify(inventoryService).operate(argThat(op -> op.getBatchId().equals(31L) && op.getQuantity().compareTo(new BigDecimal("2")) == 0));
        verify(inventoryService).operate(argThat(op -> op.getBatchId().equals(32L) && op.getQuantity().compareTo(new BigDecimal("1")) == 0));
        verify(dispenseItemMapper, times(2)).insert(any());
    }

    private static InventoryBatch batch(Long id, LocalDate expiry, BigDecimal available) {
        InventoryBatch batch = new InventoryBatch();
        batch.setId(id); batch.setDrugId(1L); batch.setWarehouseCode("OPD"); batch.setBatchNo("B" + id);
        batch.setExpiryDate(expiry); batch.setAvailableQuantity(available);
        return batch;
    }

    private static DispenseCreateRequest request() {
        DispenseItemRequest item = new DispenseItemRequest();
        item.setDrugId(1L); item.setQuantity(new BigDecimal("3")); item.setUnit("盒");
        DispenseCreateRequest request = new DispenseCreateRequest();
        request.setPrescriptionId(6L); request.setRxReviewId(7L); request.setPatientId(5L);
        request.setWarehouseCode("OPD"); request.setItems(List.of(item));
        return request;
    }
}
