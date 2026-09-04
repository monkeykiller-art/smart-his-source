package com.smarthis.operations.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.operations.converter.SettlementConverter;
import com.smarthis.operations.dto.request.SettlementCancelRequest;
import com.smarthis.operations.dto.request.SettlementCreateRequest;
import com.smarthis.operations.dto.request.SettlementQueryRequest;
import com.smarthis.operations.dto.response.SettlementItemVo;
import com.smarthis.operations.dto.response.SettlementPreviewVo;
import com.smarthis.operations.dto.response.SettlementVo;
import com.smarthis.operations.entity.*;
import com.smarthis.operations.enums.BillStatus;
import com.smarthis.operations.enums.SettleType;
import com.smarthis.operations.enums.VisitType;
import com.smarthis.operations.enums.PayMethod;
import com.smarthis.operations.mapper.*;
import com.smarthis.operations.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementMapper settlementMapper;
    private final SettlementItemMapper settlementItemMapper;
    private final BillMapper billMapper;
    private final BillItemMapper billItemMapper;
    private final DepositAccountMapper depositAccountMapper;
    private final FeeItemMapper feeItemMapper;
    private final BizNoGenerator bizNoGenerator;

    @Override
    @Transactional
    public SettlementVo create(SettlementCreateRequest request) {
        // 1. Find bills for the admission
        LambdaQueryWrapper<Bill> bq = new LambdaQueryWrapper<>();
        bq.eq(Bill::getDeleted, 0).eq(Bill::getAdmissionId, request.getAdmissionId())
          .eq(Bill::getBillStatus, BillStatus.UNSETTLED);
        List<Bill> bills = billMapper.selectList(bq);
        if (bills.isEmpty()) throw new BusinessException(ErrorCode.BILL_NOT_FOUND);

        // 2. Collect all bill items
        List<BillItem> allItems = new ArrayList<>();
        for (Bill bill : bills) {
            LambdaQueryWrapper<BillItem> iq = new LambdaQueryWrapper<>();
            iq.eq(BillItem::getBillId, bill.getId()).eq(BillItem::getDeleted, 0)
              .eq(BillItem::getIsRefunded, 0);
            allItems.addAll(billItemMapper.selectList(iq));
        }

        // 3. Calculate total
        BigDecimal totalAmount = allItems.stream().map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Calculate insurance from fee items
        BigDecimal insuranceAmount = BigDecimal.ZERO;
        Map<String, List<BillItem>> byClass = allItems.stream()
                .collect(Collectors.groupingBy(i -> i.getItemClass() != null ? i.getItemClass() : "OTHER"));
        List<SettlementItem> settleItems = new ArrayList<>();
        for (Map.Entry<String, List<BillItem>> entry : byClass.entrySet()) {
            BigDecimal classTotal = entry.getValue().stream().map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal classInsurance = calcInsurance(entry.getValue());
            insuranceAmount = insuranceAmount.add(classInsurance);
            SettlementItem si = new SettlementItem();
            si.setItemClass(entry.getKey());
            si.setItemCount(entry.getValue().size());
            si.setTotalAmount(classTotal);
            si.setInsuranceAmount(classInsurance);
            si.setSelfPayAmount(classTotal.subtract(classInsurance));
            settleItems.add(si);
        }

        // 5. Deduct deposit
        BigDecimal depositAmount = BigDecimal.ZERO;
        LambdaQueryWrapper<DepositAccount> daq = new LambdaQueryWrapper<>();
        daq.eq(DepositAccount::getPatientId, request.getPatientId())
           .eq(DepositAccount::getAdmissionId, request.getAdmissionId())
           .eq(DepositAccount::getDeleted, 0);
        DepositAccount account = depositAccountMapper.selectOne(daq);
        if (account != null && account.getBalance() != null && account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            depositAmount = account.getBalance().min(totalAmount.subtract(insuranceAmount));
            account.setTotalCharged(safeAdd(account.getTotalCharged(), depositAmount));
            account.setBalance(account.getBalance().subtract(depositAmount));
            account.setFrozenAmount(safeAdd(account.getFrozenAmount(), depositAmount));
            depositAccountMapper.updateById(account);
        }

        // 6. Self pay = total - insurance - deposit
        BigDecimal selfPayAmount = totalAmount.subtract(insuranceAmount).subtract(depositAmount);
        if (selfPayAmount.compareTo(BigDecimal.ZERO) < 0) selfPayAmount = BigDecimal.ZERO;

        // 7. Create settlement
        Settlement settle = new Settlement();
        settle.setSettleNo(bizNoGenerator.next(BizNoType.SETTLEMENT));
        settle.setPatientId(request.getPatientId());
        settle.setAdmissionId(request.getAdmissionId());
        settle.setEncounterId(request.getEncounterId());
        settle.setVisitType(request.getVisitType() != null ? VisitType.valueOf(request.getVisitType()) : null);
        settle.setPatientType(request.getPatientType());
        settle.setTotalAmount(totalAmount);
        settle.setInsuranceAmount(insuranceAmount);
        settle.setDepositAmount(depositAmount);
        settle.setSelfPayAmount(selfPayAmount);
        settle.setSettleBalance(BigDecimal.ZERO);
        settle.setSettleType(request.getSettleType() != null ? SettleType.valueOf(request.getSettleType()) : SettleType.FINAL);
        settle.setPayMethod(request.getPayMethod() != null ? PayMethod.valueOf(request.getPayMethod()) : null);
        settle.setCashierId(request.getCashierId());
        settle.setCashierName(request.getCashierName());
        settle.setSettleTime(LocalDateTime.now());
        settle.setSettleStatus("SETTLED");
        settle.setRemark(request.getRemark());
        settlementMapper.insert(settle);

        // 8. Create settlement items
        for (SettlementItem si : settleItems) {
            si.setSettlementId(settle.getId());
            settlementItemMapper.insert(si);
        }

        // 9. Update bill statuses
        for (Bill bill : bills) {
            bill.setBillStatus(BillStatus.SETTLED);
            billMapper.updateById(bill);
        }

        log.info("Settlement created: settleNo={}, total={}", settle.getSettleNo(), totalAmount);
        SettlementVo vo = SettlementConverter.toVo(settle);
        vo.setItems(SettlementConverter.toItemVoList(settleItems));
        return vo;
    }

    @Override
    public SettlementVo getById(Long id) {
        Settlement s = settlementMapper.selectById(id);
        if (s == null || s.getDeleted() != 0) throw new BusinessException(ErrorCode.BILL_NOT_FOUND);
        SettlementVo vo = SettlementConverter.toVo(s);
        LambdaQueryWrapper<SettlementItem> q = new LambdaQueryWrapper<>();
        q.eq(SettlementItem::getSettlementId, id).eq(SettlementItem::getDeleted, 0);
        List<SettlementItem> items = settlementItemMapper.selectList(q);
        vo.setItems(SettlementConverter.toItemVoList(items));
        return vo;
    }

    @Override
    public PageResult<SettlementVo> query(SettlementQueryRequest request) {
        LambdaQueryWrapper<Settlement> query = new LambdaQueryWrapper<>();
        query.eq(Settlement::getDeleted, 0);
        if (request.getPatientId() != null) query.eq(Settlement::getPatientId, request.getPatientId());
        if (request.getAdmissionId() != null) query.eq(Settlement::getAdmissionId, request.getAdmissionId());
        if (StringUtils.hasText(request.getSettleStatus())) query.eq(Settlement::getSettleStatus, request.getSettleStatus());
        if (StringUtils.hasText(request.getVisitType())) query.eq(Settlement::getVisitType, VisitType.valueOf(request.getVisitType()));
        query.orderByDesc(Settlement::getCreatedTime);
        Page<Settlement> page = request.toPage();
        IPage<Settlement> result = settlementMapper.selectPage(page, query);
        List<SettlementVo> vos = result.getRecords().stream().map(SettlementConverter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public SettlementVo cancel(Long id, SettlementCancelRequest request) {
        Settlement s = settlementMapper.selectById(id);
        if (s == null || s.getDeleted() != 0) throw new BusinessException(ErrorCode.BILL_NOT_FOUND);
        s.setSettleStatus("CANCELLED");
        s.setCancelTime(LocalDateTime.now());
        s.setCancelBy(request.getCancelBy());
        s.setCancelReason(request.getCancelReason());
        settlementMapper.updateById(s);
        log.info("Settlement cancelled: id={}", id);
        return getById(id);
    }

    @Override
    public SettlementPreviewVo preview(Long admissionId) {
        LambdaQueryWrapper<Bill> bq = new LambdaQueryWrapper<>();
        bq.eq(Bill::getDeleted, 0).eq(Bill::getAdmissionId, admissionId)
          .in(Bill::getBillStatus, BillStatus.UNSETTLED, BillStatus.PARTIAL);
        List<Bill> bills = billMapper.selectList(bq);
        BigDecimal total = BigDecimal.ZERO;
        List<BillItem> allItems = new ArrayList<>();
        for (Bill bill : bills) {
            LambdaQueryWrapper<BillItem> iq = new LambdaQueryWrapper<>();
            iq.eq(BillItem::getBillId, bill.getId()).eq(BillItem::getDeleted, 0).eq(BillItem::getIsRefunded, 0);
            List<BillItem> items = billItemMapper.selectList(iq);
            allItems.addAll(items);
            total = total.add(items.stream().map(BillItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        BigDecimal insurance = BigDecimal.ZERO;
        for (BillItem bi : allItems) {
            if (bi.getFeeItemId() != null) {
                FeeItem fi = feeItemMapper.selectById(bi.getFeeItemId());
                if (fi != null && fi.getIsInsurance() != null && fi.getIsInsurance() == 1 && fi.getInsuranceRatio() != null) {
                    insurance = insurance.add(bi.getAmount().multiply(fi.getInsuranceRatio()).divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP));
                }
            }
        }
        SettlementPreviewVo vo = SettlementConverter.toPreviewVo(null, admissionId);
        vo.setTotalAmount(total);
        vo.setInsuranceAmount(insurance);
        vo.setDepositAmount(BigDecimal.ZERO);
        vo.setSelfPayAmount(total.subtract(insurance));
        return vo;
    }

    private BigDecimal calcInsurance(List<BillItem> items) {
        BigDecimal result = BigDecimal.ZERO;
        for (BillItem bi : items) {
            if (bi.getFeeItemId() != null) {
                FeeItem fi = feeItemMapper.selectById(bi.getFeeItemId());
                if (fi != null && fi.getIsInsurance() != null && fi.getIsInsurance() == 1 && fi.getInsuranceRatio() != null) {
                    result = result.add(bi.getAmount().multiply(fi.getInsuranceRatio()).setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
        return result;
    }

    private BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        return (a != null ? a : BigDecimal.ZERO).add(b != null ? b : BigDecimal.ZERO);
    }
}
