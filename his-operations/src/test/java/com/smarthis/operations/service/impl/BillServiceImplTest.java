package com.smarthis.operations.service.impl;

import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.operations.entity.Bill;
import com.smarthis.operations.entity.BillItem;
import com.smarthis.operations.mapper.BillItemMapper;
import com.smarthis.operations.mapper.BillMapper;
import com.smarthis.operations.mapper.BillTransactionMapper;
import com.smarthis.operations.mapper.FeeItemMapper;
import com.smarthis.operations.enums.BillStatus;
import com.smarthis.operations.dto.request.BillPaymentRequest;
import com.smarthis.operations.dto.request.BillRefundRequest;
import com.smarthis.operations.dto.request.BillVoidRequest;
import com.smarthis.operations.dto.response.BillTransactionVo;
import com.smarthis.operations.entity.BillTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BillServiceImplTest {

    private final BillMapper billMapper = mock(BillMapper.class);
    private final BillItemMapper billItemMapper = mock(BillItemMapper.class);
    private final FeeItemMapper feeItemMapper = mock(FeeItemMapper.class);
    private final BillTransactionMapper billTransactionMapper = mock(BillTransactionMapper.class);
    private final BillServiceImpl service = new BillServiceImpl(
            billMapper, billItemMapper, feeItemMapper, mock(BizNoGenerator.class), billTransactionMapper);

    @Test
    void listsBillItemsOnlyForAnExistingBill() {
        Bill bill = new Bill();
        bill.setId(42L);
        bill.setDeleted(0);
        when(billMapper.selectById(42L)).thenReturn(bill);
        BillItem item = new BillItem();
        item.setId(7L);
        item.setBillId(42L);
        item.setItemSeq(1);
        item.setItemName("门诊诊查费");
        item.setUnitPrice(new BigDecimal("12.00"));
        item.setQuantity(new BigDecimal("1.00"));
        item.setAmount(new BigDecimal("12.00"));
        when(billItemMapper.selectList(any())).thenReturn(List.of(item));

        var result = service.listItems(42L);

        assertEquals(1, result.size());
        assertEquals("门诊诊查费", result.getFirst().getItemName());
        assertEquals(new BigDecimal("12.00"), result.getFirst().getAmount());
        verify(billItemMapper).selectList(any());
    }

    @Test
    void rejectsItemLookupForMissingBill() {
        when(billMapper.selectById(404L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.listItems(404L));
        verifyNoInteractions(billItemMapper);
    }

    @Test
    void recordsPartialPaymentAndUpdatesBillBalance() {
        Bill bill = bill(42L, "100.00", "0.00", BillStatus.UNSETTLED);
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        when(billTransactionMapper.selectOne(any())).thenReturn(null);
        when(billTransactionMapper.insert(any(BillTransaction.class))).thenAnswer(invocation -> {
            BillTransaction transaction = invocation.getArgument(0);
            transaction.setId(9L);
            return 1;
        });
        when(billMapper.updateById(bill)).thenReturn(1);
        BillPaymentRequest request = new BillPaymentRequest();
        request.setAmount(new BigDecimal("35.2500"));
        request.setPayMethod("CASH");
        request.setIdempotencyKey("cashier-01-key-0001");

        BillTransactionVo result = service.pay(42L, request);

        assertEquals("PAYMENT", result.getTransactionType());
        assertEquals(new BigDecimal("35.2500"), bill.getPaidAmount());
        assertEquals(BillStatus.PARTIAL, bill.getBillStatus());
        verify(billTransactionMapper).insert(any(BillTransaction.class));
        verify(billMapper).updateById(bill);
    }

    @Test
    void rejectsPaymentAboveOutstandingBalance() {
        Bill bill = bill(42L, "100.00", "80.00", BillStatus.PARTIAL);
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        when(billTransactionMapper.selectOne(any())).thenReturn(null);
        BillPaymentRequest request = new BillPaymentRequest();
        request.setAmount(new BigDecimal("20.01"));
        request.setPayMethod("POS");
        request.setIdempotencyKey("cashier-01-key-0002");

        assertThrows(BusinessException.class, () -> service.pay(42L, request));
        verify(billTransactionMapper, never()).insert(any(BillTransaction.class));
        verify(billMapper, never()).updateById(any(Bill.class));
    }

    @Test
    void replaysIdenticalPaymentWithoutWritingTwice() {
        Bill bill = bill(42L, "100.00", "25.00", BillStatus.PARTIAL);
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        BillTransaction previous = new BillTransaction();
        previous.setId(9L);
        previous.setBillId(42L);
        previous.setTransactionType("PAYMENT");
        previous.setAmount(new BigDecimal("25.00"));
        previous.setPayMethod(com.smarthis.operations.enums.PayMethod.CASH);
        previous.setIdempotencyKey("cashier-01-key-0003");
        when(billTransactionMapper.selectOne(any())).thenReturn(previous);
        BillPaymentRequest request = new BillPaymentRequest();
        request.setAmount(new BigDecimal("25.0000"));
        request.setPayMethod("CASH");
        request.setIdempotencyKey("cashier-01-key-0003");

        BillTransactionVo result = service.pay(42L, request);

        assertEquals(9L, result.getId());
        assertEquals(new BigDecimal("25.00"), bill.getPaidAmount());
        verify(billTransactionMapper, never()).insert(any(BillTransaction.class));
        verify(billMapper, never()).updateById(any(Bill.class));
    }

    @Test
    void rejectsRefundAbovePaidBalance() {
        Bill bill = bill(42L, "100.00", "15.00", BillStatus.PARTIAL);
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        when(billTransactionMapper.selectOne(any())).thenReturn(null);
        BillRefundRequest request = new BillRefundRequest();
        request.setAmount(new BigDecimal("15.01"));
        request.setReason("退还多收金额");
        request.setIdempotencyKey("cashier-01-key-0004");

        assertThrows(BusinessException.class, () -> service.refund(42L, request));
        verify(billTransactionMapper, never()).insert(any(BillTransaction.class));
        verify(billMapper, never()).updateById(any(Bill.class));
    }

    @Test
    void refundsFromSettledBillAndReturnsItToPartialState() {
        Bill bill = bill(42L, "100.00", "100.00", BillStatus.SETTLED);
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        when(billTransactionMapper.selectOne(any())).thenReturn(null);
        when(billTransactionMapper.insert(any(BillTransaction.class))).thenReturn(1);
        BillRefundRequest request = new BillRefundRequest();
        request.setAmount(new BigDecimal("25.00"));
        request.setReason("重复收费退还");
        request.setIdempotencyKey("cashier-01-key-0005");

        BillTransactionVo result = service.refund(42L, request);

        assertEquals("REFUND", result.getTransactionType());
        assertEquals(new BigDecimal("75.00"), bill.getPaidAmount());
        assertEquals(BillStatus.PARTIAL, bill.getBillStatus());
        verify(billMapper).updateById(bill);
    }

    @Test
    void voidsOnlyUnpaidBillsAndStoresTheReasonSeparately() {
        Bill bill = bill(42L, "100.00", "0.00", BillStatus.UNSETTLED);
        bill.setRemark("原始收费说明");
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        when(billMapper.updateById(bill)).thenReturn(1);
        BillVoidRequest request = new BillVoidRequest();
        request.setReason("重复开单");

        var result = service.voidBill(42L, request);

        assertEquals(BillStatus.CANCELLED, bill.getBillStatus());
        assertEquals("重复开单", result.getVoidReason());
        assertEquals("原始收费说明", bill.getRemark());
        verify(billMapper).updateById(bill);
    }

    @Test
    void refusesToVoidPartiallyPaidBills() {
        Bill bill = bill(42L, "100.00", "1.00", BillStatus.PARTIAL);
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        BillVoidRequest request = new BillVoidRequest();
        request.setReason("误开单");

        assertThrows(BusinessException.class, () -> service.voidBill(42L, request));
        verify(billMapper, never()).updateById(any(Bill.class));
    }

    private Bill bill(Long id, String payable, String paid, BillStatus status) {
        Bill bill = new Bill();
        bill.setId(id);
        bill.setDeleted(0);
        bill.setPayableAmount(new BigDecimal(payable));
        bill.setPaidAmount(new BigDecimal(paid));
        bill.setBillStatus(status);
        return bill;
    }
}
