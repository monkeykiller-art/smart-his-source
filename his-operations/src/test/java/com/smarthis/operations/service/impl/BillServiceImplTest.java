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
import com.smarthis.operations.entity.FeeItem;
import com.smarthis.operations.dto.request.BillChargeItemRequest;
import com.smarthis.operations.dto.request.BillRegistrationRequest;
import com.smarthis.operations.dto.request.BillOrderRequest;
import com.smarthis.operations.dto.request.BillOrderCancelRequest;
import com.smarthis.operations.dto.request.BillCreateRequest;
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
    void admissionBillingCreatesOneReusableInpatientAccount() {
        BillCreateRequest request = new BillCreateRequest();
        request.setAdmissionId(81L);
        request.setPatientId(10L);
        request.setDeptId(20L);
        when(billMapper.insert(any(Bill.class))).thenAnswer(invocation -> { ((Bill) invocation.getArgument(0)).setId(91L); return 1; });

        var created = service.createFromAdmission(request);

        assertEquals(91L, created.getId());
        assertEquals("INPATIENT", created.getVisitType());
        verify(billMapper).insert(argThat(bill -> "ADMISSION".equals(bill.getSourceType()) && bill.getSourceId().equals(81L)));
    }

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

    @Test
    void voidsZeroPriceSettledBillWithoutRefundingMoney() {
        Bill bill = bill(42L, "0", "0", BillStatus.SETTLED);
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        BillVoidRequest request = new BillVoidRequest();
        request.setReason("免费项目取消");
        assertEquals("CANCELLED", service.voidBill(42L, request).getBillStatus());
    }

    @Test
    void refusesManualChangesToGeneratedBusinessSourceSnapshot() {
        Bill bill = bill(42L, "10", "0", BillStatus.UNSETTLED);
        bill.setSourceType("ORDER");
        bill.setSourceId(81L);
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        assertThrows(BusinessException.class, () -> service.addChargeItem(42L, chargeRequest(BigDecimal.ONE)));
        verifyNoInteractions(feeItemMapper, billItemMapper);
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

    @Test
    void addsChargeUnderBillLockWithNextSequenceAndExactBalance() {
        Bill bill = bill(42L, "10.00", "5.00", BillStatus.PARTIAL);
        when(billMapper.selectByIdForUpdate(42L)).thenReturn(bill);
        FeeItem fee = feeItem();
        when(feeItemMapper.selectById(7L)).thenReturn(fee);
        BillItem last = new BillItem();
        last.setItemSeq(3);
        when(billItemMapper.selectOne(any())).thenReturn(last);
        BillItem previous = new BillItem();
        previous.setAmount(new BigDecimal("10.00"));
        when(billItemMapper.insert(any(BillItem.class))).thenAnswer(invocation -> {
            BillItem added = invocation.getArgument(0);
            assertEquals(4, added.getItemSeq());
            assertEquals(new BigDecimal("0.30"), added.getAmount());
            when(billItemMapper.selectList(any())).thenReturn(List.of(previous, added));
            return 1;
        });
        service.addChargeItem(42L, chargeRequest(new BigDecimal("3")));
        assertEquals(new BigDecimal("10.30"), bill.getPayableAmount());
        assertEquals(new BigDecimal("5.00"), bill.getPaidAmount());
        verify(billMapper).selectByIdForUpdate(42L);
        verify(billMapper, never()).selectById(any());
    }

    @Test
    void refusesChargesOnSettledBillBeforeWritingItems() {
        when(billMapper.selectByIdForUpdate(42L))
                .thenReturn(bill(42L, "10.00", "10.00", BillStatus.SETTLED));
        assertThrows(BusinessException.class,
                () -> service.addChargeItem(42L, chargeRequest(BigDecimal.ONE)));
        verifyNoInteractions(feeItemMapper, billItemMapper);
    }

    @Test
    void rejectsNegativeChargeQuantityAndPriceWithoutWriting() {
        when(billMapper.selectByIdForUpdate(42L))
                .thenReturn(bill(42L, "10.00", "0.00", BillStatus.UNSETTLED));
        when(feeItemMapper.selectById(7L)).thenReturn(feeItem());
        assertThrows(BusinessException.class,
                () -> service.addChargeItem(42L, chargeRequest(new BigDecimal("-1"))));
        BillChargeItemRequest request = chargeRequest(BigDecimal.ONE);
        request.setUnitPrice(new BigDecimal("-0.10"));
        assertThrows(BusinessException.class, () -> service.addChargeItem(42L, request));
        verify(billItemMapper, never()).insert(any(BillItem.class));
        verify(billMapper, never()).updateById(any(Bill.class));
    }

    private FeeItem feeItem() {
        FeeItem fee = new FeeItem();
        fee.setId(7L);
        fee.setDeleted(0);
        fee.setUnitPrice(new BigDecimal("0.10"));
        return fee;
    }

    @Test
    void createsRegistrationBillWithMatchingChargeItemAndSource() {
        when(billMapper.insert(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            bill.setId(91L);
            return 1;
        });
        var result = service.createFromRegistration(registrationRequest());
        assertEquals(91L, result.getId());
        assertEquals("OUTPATIENT", result.getVisitType());
        assertEquals(new BigDecimal("12.50"), result.getPayableAmount());
        assertEquals("UNSETTLED", result.getBillStatus());
        verify(billItemMapper).insert(argThat((BillItem item) -> item.getBillId().equals(91L)
                && item.getAmount().compareTo(new BigDecimal("12.50")) == 0
                && "REGISTRATION".equals(item.getItemClass())));
        verify(billMapper).insert(argThat((Bill bill) -> "REGISTRATION".equals(bill.getSourceType())
                && bill.getSourceId().equals(81L)));
    }

    @Test
    void replaysRegistrationBillAndRejectsChangedAmountWithoutCreatingAnotherBill() {
        Bill previous = bill(91L, "12.50", "12.50", BillStatus.SETTLED);
        previous.setPatientId(10L);
        previous.setTotalAmount(new BigDecimal("12.50"));
        when(billMapper.selectOne(any())).thenReturn(previous);
        assertEquals(91L, service.createFromRegistration(registrationRequest()).getId());
        BillRegistrationRequest changed = registrationRequest();
        changed.setAmount(new BigDecimal("13.00"));
        assertThrows(BusinessException.class, () -> service.createFromRegistration(changed));
        verify(billMapper, never()).insert(any(Bill.class));
        verifyNoInteractions(billItemMapper);
    }

    @Test
    void rejectsMissingAndNegativeRegistrationAmountBeforeLockOrWrite() {
        BillRegistrationRequest request = registrationRequest();
        request.setAmount(null);
        assertThrows(BusinessException.class, () -> service.createFromRegistration(request));
        request.setAmount(new BigDecimal("-1"));
        assertThrows(BusinessException.class, () -> service.createFromRegistration(request));
        verifyNoInteractions(billMapper, billItemMapper);
    }

    private BillRegistrationRequest registrationRequest() {
        BillRegistrationRequest request = new BillRegistrationRequest();
        request.setRegId(81L);
        request.setPatientId(10L);
        request.setEncounterId(82L);
        request.setAmount(new BigDecimal("12.50"));
        return request;
    }

    @Test
    void createsExactOrderChargesAndReplaysWithoutDuplicateItems() {
        java.util.List<BillItem> stored = new java.util.ArrayList<>();
        java.util.concurrent.atomic.AtomicReference<Bill> bill = new java.util.concurrent.atomic.AtomicReference<>();
        when(billMapper.insert(any(Bill.class))).thenAnswer(invocation -> {
            Bill created = invocation.getArgument(0);
            created.setId(91L);
            bill.set(created);
            return 1;
        });
        when(billItemMapper.insert(any(BillItem.class))).thenAnswer(invocation -> {
            stored.add(invocation.getArgument(0));
            return 1;
        });
        when(billMapper.selectOne(any())).thenAnswer(invocation -> bill.get());
        when(billItemMapper.selectList(any())).thenReturn(stored);
        BillOrderRequest request = orderRequest();
        assertEquals(new BigDecimal("0.30"), service.createFromOrder(request).getPayableAmount());
        assertEquals(91L, service.createFromOrder(request).getId());
        assertEquals(1, stored.size());
        assertEquals(82L, stored.getFirst().getOrderItemId());
        assertEquals("ORDER", bill.get().getSourceType());
        verify(billMapper, times(1)).insert(any(Bill.class));
        request.getItems().getFirst().setItemName("changed item");
        assertThrows(BusinessException.class, () -> service.createFromOrder(request));
    }

    @Test
    void rejectsInvalidOrderAmountBeforePersistingAnyBill() {
        BillOrderRequest request = orderRequest();
        request.getItems().getFirst().setQuantity(new BigDecimal("-1"));
        assertThrows(BusinessException.class, () -> service.createFromOrder(request));
        verifyNoInteractions(billMapper, billItemMapper);
    }

    @Test
    void replayingVoidPreservesOriginalReasonWithoutAnotherWrite() {
        Bill cancelled = bill(91L, "0.30", "0", BillStatus.CANCELLED);
        cancelled.setVoidReason("original reason");
        when(billMapper.selectByIdForUpdate(91L)).thenReturn(cancelled);
        BillVoidRequest request = new BillVoidRequest();
        request.setReason("retry");
        assertEquals("original reason", service.voidBill(91L, request).getVoidReason());
        verify(billMapper, never()).updateById(any(Bill.class));
    }

    private BillOrderRequest orderRequest() {
        BillOrderRequest request = new BillOrderRequest();
        request.setOrderId(81L);
        request.setPatientId(10L);
        request.setEncounterId(31L);
        request.setDeptId(20L);
        BillOrderRequest.Item line = new BillOrderRequest.Item();
        line.setOrderItemId(82L);
        line.setItemName("测试检验");
        line.setUnitPrice(new BigDecimal("0.10"));
        line.setQuantity(new BigDecimal("3"));
        request.setItems(List.of(line));
        return request;
    }

    @Test
    void cancellationIntentBlocksLateOrderBillingEvenIfNoBillWasReturned() {
        java.util.concurrent.atomic.AtomicReference<Bill> source = new java.util.concurrent.atomic.AtomicReference<>();
        when(billMapper.selectOne(any())).thenAnswer(invocation -> source.get());
        when(billMapper.insert(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            bill.setId(91L);
            source.set(bill);
            return 1;
        });
        BillOrderCancelRequest request = new BillOrderCancelRequest();
        request.setOrderId(81L);
        request.setPatientId(10L);
        request.setEncounterId(31L);
        request.setDeptId(20L);
        request.setReason("医生撤销");
        assertEquals("CANCELLED", service.voidOrderSource(request).getBillStatus());
        assertThrows(BusinessException.class, () -> service.createFromOrder(orderRequest()));
        verify(billMapper, times(1)).insert(any(Bill.class));
        verifyNoInteractions(billItemMapper);
    }

    private BillChargeItemRequest chargeRequest(BigDecimal quantity) {
        BillChargeItemRequest request = new BillChargeItemRequest();
        request.setBillId(42L);
        request.setFeeItemId(7L);
        request.setQuantity(quantity);
        return request;
    }
}
