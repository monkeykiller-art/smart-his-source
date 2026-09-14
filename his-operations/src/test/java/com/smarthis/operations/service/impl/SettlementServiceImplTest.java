package com.smarthis.operations.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.operations.entity.Bill;
import com.smarthis.operations.entity.BillItem;
import com.smarthis.operations.entity.DepositAccount;
import com.smarthis.operations.mapper.BillItemMapper;
import com.smarthis.operations.mapper.BillMapper;
import com.smarthis.operations.mapper.DepositAccountMapper;
import com.smarthis.operations.mapper.FeeItemMapper;
import com.smarthis.operations.mapper.SettlementItemMapper;
import com.smarthis.operations.mapper.SettlementMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettlementServiceImplTest {

    private final SettlementMapper settlementMapper = mock(SettlementMapper.class);
    private final SettlementItemMapper settlementItemMapper = mock(SettlementItemMapper.class);
    private final BillMapper billMapper = mock(BillMapper.class);
    private final BillItemMapper billItemMapper = mock(BillItemMapper.class);
    private final DepositAccountMapper depositAccountMapper = mock(DepositAccountMapper.class);
    private final FeeItemMapper feeItemMapper = mock(FeeItemMapper.class);
    private final BizNoGenerator bizNoGenerator = mock(BizNoGenerator.class);
    private final SettlementServiceImpl service = new SettlementServiceImpl(
            settlementMapper, settlementItemMapper, billMapper, billItemMapper,
            depositAccountMapper, feeItemMapper, bizNoGenerator);

    @Test
    void previewReturnsActualPatientAndDepositAdjustedSelfPayForUnsettledBills() {
        Bill bill = new Bill();
        bill.setPatientId(12L);
        bill.setAdmissionId(44L);
        BillItem item = new BillItem();
        item.setAmount(new BigDecimal("100.00"));
        item.setFeeItemId(null);
        DepositAccount account = new DepositAccount();
        account.setBalance(new BigDecimal("30.00"));
        when(billMapper.selectList(any(Wrapper.class))).thenReturn(List.of(bill));
        when(billItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(item));
        when(depositAccountMapper.selectOne(any(Wrapper.class))).thenReturn(account);

        var preview = service.preview(44L);

        assertEquals(12L, preview.getPatientId());
        assertEquals(44L, preview.getAdmissionId());
        assertEquals(new BigDecimal("100.00"), preview.getTotalAmount());
        assertEquals(new BigDecimal("30.00"), preview.getDepositAmount());
        assertEquals(new BigDecimal("30.00"), preview.getDepositBalance());
        assertEquals(new BigDecimal("70.00"), preview.getSelfPayAmount());
    }

    @Test
    void previewRejectsAdmissionWithoutUnsettledBills() {
        when(billMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> service.preview(44L));
    }
}
