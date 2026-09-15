package com.smarthis.operations.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthis.common.exception.BusinessException;
import com.smarthis.common.model.ErrorCode;
import com.smarthis.common.model.PageResult;
import com.smarthis.common.support.BizNoGenerator;
import com.smarthis.common.support.BizNoType;
import com.smarthis.operations.converter.BillConverter;
import com.smarthis.operations.dto.request.*;
import com.smarthis.operations.dto.response.BillItemVo;
import com.smarthis.operations.dto.response.BillTransactionVo;
import com.smarthis.operations.dto.response.BillVo;
import com.smarthis.operations.entity.Bill;
import com.smarthis.operations.entity.BillItem;
import com.smarthis.operations.entity.BillTransaction;
import com.smarthis.operations.entity.FeeItem;
import com.smarthis.operations.enums.BillStatus;
import com.smarthis.operations.enums.BillType;
import com.smarthis.operations.enums.PayMethod;
import com.smarthis.operations.enums.VisitType;
import com.smarthis.operations.mapper.BillItemMapper;
import com.smarthis.operations.mapper.BillMapper;
import com.smarthis.operations.mapper.BillTransactionMapper;
import com.smarthis.operations.mapper.FeeItemMapper;
import com.smarthis.operations.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;
    private final BillItemMapper billItemMapper;
    private final FeeItemMapper feeItemMapper;
    private final BizNoGenerator bizNoGenerator;
    private final BillTransactionMapper billTransactionMapper;

    @Override
    @Transactional
    public BillVo create(BillCreateRequest request) {
        Bill bill = newBill(bizNoGenerator.next(BizNoType.BILL), request.getPatientId(),
                request.getAdmissionId(), request.getEncounterId(), request.getVisitType(),
                request.getDeptId(), request.getBillType(), request.getRemark());
        billMapper.insert(bill);
        log.info("Bill created: billNo={}", bill.getBillNo());
        return BillConverter.toVo(bill);
    }

    @Override
    public BillVo getById(Long id) {
        Bill bill = requireBill(id);
        return BillConverter.toVo(bill);
    }

    @Override
    public List<BillItemVo> listItems(Long id) {
        requireBill(id);
        return BillConverter.toItemVoList(listItemsForBill(id));
    }

    @Override
    public List<BillTransactionVo> listTransactions(Long id) {
        requireBill(id);
        LambdaQueryWrapper<BillTransaction> query = new LambdaQueryWrapper<>();
        query.eq(BillTransaction::getBillId, id).eq(BillTransaction::getDeleted, 0)
                .orderByDesc(BillTransaction::getTransactionTime);
        return billTransactionMapper.selectList(query).stream().map(this::toTransactionVo).toList();
    }

    @Override
    @Transactional
    public BillTransactionVo pay(Long id, BillPaymentRequest request) {
        Bill bill = requireLockedBill(id);
        BigDecimal amount = validateMoney(request.getAmount());
        BillTransaction previous = findByIdempotencyKey(request.getIdempotencyKey());
        if (previous != null) {
            ensureSameTransaction(previous, id, "PAYMENT", amount, request.getPayMethod(),
                    request.getReferenceNo(), null);
            return toTransactionVo(previous);
        }
        if (bill.getBillStatus() == BillStatus.CANCELLED || bill.getBillStatus() == BillStatus.SETTLED) {
            throw new BusinessException(ErrorCode.BILL_STATUS_INVALID);
        }
        BigDecimal paid = zeroIfNull(bill.getPaidAmount());
        BigDecimal due = zeroIfNull(bill.getPayableAmount()).subtract(paid);
        if (amount.compareTo(due) > 0) throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);

        BillTransaction transaction = newTransaction(id, "PAYMENT", amount,
                PayMethod.valueOf(request.getPayMethod()), request.getReferenceNo(),
                request.getIdempotencyKey(), null, BizNoType.PAYMENT);
        billTransactionMapper.insert(transaction);
        bill.setPaidAmount(paid.add(amount));
        bill.setBillStatus(bill.getPaidAmount().compareTo(bill.getPayableAmount()) >= 0
                ? BillStatus.SETTLED : BillStatus.PARTIAL);
        billMapper.updateById(bill);
        log.info("Bill payment recorded: billId={}, transactionNo={}, amount={}", id, transaction.getTransactionNo(), amount);
        return toTransactionVo(transaction);
    }

    @Override
    @Transactional
    public BillTransactionVo refund(Long id, BillRefundRequest request) {
        Bill bill = requireLockedBill(id);
        BigDecimal amount = validateMoney(request.getAmount());
        BillTransaction previous = findByIdempotencyKey(request.getIdempotencyKey());
        if (previous != null) {
            ensureSameTransaction(previous, id, "REFUND", amount, null, null, request.getReason().trim());
            return toTransactionVo(previous);
        }
        if (bill.getBillStatus() == BillStatus.CANCELLED) throw new BusinessException(ErrorCode.BILL_STATUS_INVALID);
        BigDecimal paid = zeroIfNull(bill.getPaidAmount());
        if (amount.compareTo(paid) > 0) throw new BusinessException(ErrorCode.REFUND_AMOUNT_EXCEEDED);

        BillTransaction transaction = newTransaction(id, "REFUND", amount, null, null,
                request.getIdempotencyKey(), request.getReason().trim(), BizNoType.REFUND);
        billTransactionMapper.insert(transaction);
        bill.setPaidAmount(paid.subtract(amount));
        bill.setBillStatus(bill.getPaidAmount().signum() == 0
                ? BillStatus.UNSETTLED : BillStatus.PARTIAL);
        billMapper.updateById(bill);
        log.info("Bill refund recorded: billId={}, transactionNo={}, amount={}", id, transaction.getTransactionNo(), amount);
        return toTransactionVo(transaction);
    }

    @Override
    @Transactional
    public BillVo voidBill(Long id, BillVoidRequest request) {
        Bill bill = requireLockedBill(id);
        if (bill.getBillStatus() != BillStatus.UNSETTLED || zeroIfNull(bill.getPaidAmount()).signum() > 0) {
            throw new BusinessException(ErrorCode.BILL_STATUS_INVALID);
        }
        bill.setBillStatus(BillStatus.CANCELLED);
        bill.setVoidReason(request.getReason().trim());
        billMapper.updateById(bill);
        log.info("Bill voided: billId={}", id);
        return BillConverter.toVo(bill);
    }

    @Override
    public PageResult<BillVo> query(BillQueryRequest request) {
        LambdaQueryWrapper<Bill> query = new LambdaQueryWrapper<>();
        query.eq(Bill::getDeleted, 0);
        if (request.getPatientId() != null) query.eq(Bill::getPatientId, request.getPatientId());
        if (request.getAdmissionId() != null) query.eq(Bill::getAdmissionId, request.getAdmissionId());
        if (StringUtils.hasText(request.getBillStatus())) query.eq(Bill::getBillStatus, BillStatus.valueOf(request.getBillStatus()));
        if (StringUtils.hasText(request.getVisitType())) query.eq(Bill::getVisitType, VisitType.valueOf(request.getVisitType()));
        query.orderByDesc(Bill::getCreatedTime);
        Page<Bill> page = request.toPage();
        IPage<Bill> result = billMapper.selectPage(page, query);
        List<BillVo> vos = result.getRecords().stream().map(BillConverter::toVo).toList();
        return new PageResult<>(vos, result.getTotal(), request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public BillVo createFromRegistration(BillRegistrationRequest request) {
        BigDecimal amount = request.getAmount();
        if (request.getRegId() == null || request.getRegId() <= 0 || amount == null
                || amount.signum() < 0 || amount.scale() > 4 || amount.precision() - amount.scale() > 14) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        billMapper.lockSource("REGISTRATION:" + request.getRegId());
        LambdaQueryWrapper<Bill> sourceQuery = new LambdaQueryWrapper<>();
        sourceQuery.eq(Bill::getSourceType, "REGISTRATION").eq(Bill::getSourceId, request.getRegId());
        Bill existing = billMapper.selectOne(sourceQuery);
        if (existing != null) {
            if (!java.util.Objects.equals(existing.getPatientId(), request.getPatientId())
                    || zeroIfNull(existing.getTotalAmount()).compareTo(amount) != 0) {
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }
            return BillConverter.toVo(existing);
        }
        Bill bill = newBill(bizNoGenerator.next(BizNoType.BILL), request.getPatientId(),
                null, request.getEncounterId(), "OUTPATIENT",
                request.getDeptId(), request.getBillType(), request.getRemark());
        bill.setSourceType("REGISTRATION");
        bill.setSourceId(request.getRegId());
        bill.setTotalAmount(amount);
        bill.setPayableAmount(amount);
        if (amount.signum() == 0) bill.setBillStatus(BillStatus.SETTLED);
        billMapper.insert(bill);
        BillItem item = new BillItem();
        item.setBillId(bill.getId());
        item.setItemSeq(1);
        item.setItemCode("REGISTRATION");
        item.setItemName("门诊挂号费");
        item.setItemClass("REGISTRATION");
        item.setUnit("次");
        item.setUnitPrice(amount);
        item.setQuantity(BigDecimal.ONE);
        item.setAmount(amount);
        item.setChargeDeptId(request.getDeptId());
        item.setChargeTime(LocalDateTime.now());
        item.setIsRefunded(0);
        item.setItemStatus("NORMAL");
        item.setRemark(request.getRegNo());
        billItemMapper.insert(item);
        log.info("Bill from registration: billNo={}", bill.getBillNo());
        return BillConverter.toVo(bill);
    }

    @Override
    @Transactional
    public List<BillItemVo> addChargeItem(Long billId, BillChargeItemRequest request) {
        Bill bill = requireActiveBill(billId);
        FeeItem feeItem = requireFeeItem(request.getFeeItemId());
        int seq = nextSeq(billId) + 1;
        BillItem item = buildItem(billId, seq, feeItem,
                request.getItemCode(), request.getItemName(), request.getItemClass(),
                request.getSpec(), request.getUnit(), request.getUnitPrice(),
                request.getQuantity(), request.getChargeDeptId(), request.getExecuteDeptId(),
                request.getOrderId(), request.getOrderItemId(), request.getRemark());
        billItemMapper.insert(item);
        recalcBill(bill);
        log.info("Charge item added: billId={}, amount={}", billId, item.getAmount());
        return BillConverter.toItemVoList(listItemsForBill(billId));
    }

    @Override
    @Transactional
    public List<BillItemVo> chargeItems(BillChargeRequest request) {
        Bill bill = requireActiveBill(request.getBillId());
        int seq = nextSeq(request.getBillId());
        for (BillChargeRequest.ChargeItemDetail d : request.getItems()) {
            FeeItem feeItem = requireFeeItem(d.getFeeItemId());
            seq++;
            BillItem item = buildItem(request.getBillId(), seq, feeItem,
                    d.getItemCode(), d.getItemName(), d.getItemClass(),
                    d.getSpec(), d.getUnit(), d.getUnitPrice(),
                    d.getQuantity(), d.getChargeDeptId(), d.getExecuteDeptId(),
                    d.getOrderId(), d.getOrderItemId(), d.getRemark());
            billItemMapper.insert(item);
        }
        recalcBill(bill);
        log.info("Batch charge: billId={}, count={}", request.getBillId(), request.getItems().size());
        return BillConverter.toItemVoList(listItemsForBill(request.getBillId()));
    }

    // --- helpers ---

    private Bill newBill(String billNo, Long patientId, Long admissionId, Long encounterId,
                         String visitType, Long deptId, String billType, String remark) {
        Bill b = new Bill();
        b.setBillNo(billNo);
        b.setPatientId(patientId);
        b.setAdmissionId(admissionId);
        b.setEncounterId(encounterId);
        b.setVisitType(visitType != null ? VisitType.valueOf(visitType) : null);
        b.setDeptId(deptId);
        b.setTotalAmount(BigDecimal.ZERO);
        b.setDiscountAmount(BigDecimal.ZERO);
        b.setPayableAmount(BigDecimal.ZERO);
        b.setPaidAmount(BigDecimal.ZERO);
        b.setBillStatus(BillStatus.UNSETTLED);
        b.setBillType(billType != null ? BillType.valueOf(billType) : BillType.NORMAL);
        b.setRemark(remark);
        return b;
    }

    private Bill requireBill(Long id) {
        Bill b = billMapper.selectById(id);
        if (b == null || b.getDeleted() != 0) throw new BusinessException(ErrorCode.BILL_NOT_FOUND);
        return b;
    }

    private Bill requireLockedBill(Long id) {
        Bill bill = billMapper.selectByIdForUpdate(id);
        if (bill == null || bill.getDeleted() != 0) throw new BusinessException(ErrorCode.BILL_NOT_FOUND);
        return bill;
    }

    private BillTransaction findByIdempotencyKey(String key) {
        LambdaQueryWrapper<BillTransaction> query = new LambdaQueryWrapper<>();
        query.eq(BillTransaction::getIdempotencyKey, key.trim()).eq(BillTransaction::getDeleted, 0);
        return billTransactionMapper.selectOne(query);
    }

    private void ensureSameTransaction(BillTransaction existing, Long billId, String type,
            BigDecimal amount, String payMethod, String referenceNo, String reason) {
        boolean same = existing.getBillId().equals(billId)
                && existing.getTransactionType().equals(type)
                && existing.getAmount().compareTo(amount) == 0
                && java.util.Objects.equals(existing.getPayMethod() == null ? null : existing.getPayMethod().getValue(), payMethod)
                && java.util.Objects.equals(existing.getReferenceNo(), referenceNo)
                && java.util.Objects.equals(existing.getReason(), reason);
        if (!same) throw new BusinessException(ErrorCode.PAYMENT_FAILED);
    }

    private BigDecimal validateMoney(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 4 || amount.precision() > 18) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        return amount;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BillTransaction newTransaction(Long billId, String type, BigDecimal amount,
            PayMethod method, String referenceNo, String idempotencyKey, String reason, BizNoType noType) {
        BillTransaction transaction = new BillTransaction();
        transaction.setBillId(billId);
        transaction.setTransactionNo(bizNoGenerator.next(noType));
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setPayMethod(method);
        transaction.setReferenceNo(referenceNo);
        transaction.setIdempotencyKey(idempotencyKey.trim());
        transaction.setReason(reason);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTransactionStatus("SUCCESS");
        return transaction;
    }

    private BillTransactionVo toTransactionVo(BillTransaction transaction) {
        BillTransactionVo vo = new BillTransactionVo();
        vo.setId(transaction.getId());
        vo.setBillId(transaction.getBillId());
        vo.setTransactionNo(transaction.getTransactionNo());
        vo.setTransactionType(transaction.getTransactionType());
        vo.setAmount(transaction.getAmount());
        vo.setPayMethod(transaction.getPayMethod() == null ? null : transaction.getPayMethod().getValue());
        vo.setReferenceNo(transaction.getReferenceNo());
        vo.setReason(transaction.getReason());
        vo.setTransactionTime(transaction.getTransactionTime());
        vo.setTransactionStatus(transaction.getTransactionStatus());
        return vo;
    }

    private Bill requireActiveBill(Long id) {
        Bill b = requireLockedBill(id);
        if (b.getBillStatus() == BillStatus.SETTLED || b.getBillStatus() == BillStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BILL_STATUS_INVALID);
        }
        return b;
    }

    private FeeItem requireFeeItem(Long id) {
        FeeItem f = feeItemMapper.selectById(id);
        if (f == null || f.getDeleted() != 0) throw new BusinessException(ErrorCode.CHARGE_ITEM_NOT_FOUND);
        return f;
    }

    private int nextSeq(Long billId) {
        LambdaQueryWrapper<BillItem> q = new LambdaQueryWrapper<>();
        q.eq(BillItem::getBillId, billId).eq(BillItem::getDeleted, 0)
         .orderByDesc(BillItem::getItemSeq).last("LIMIT 1");
        BillItem last = billItemMapper.selectOne(q);
        return last != null && last.getItemSeq() != null ? last.getItemSeq() : 0;
    }

    private BillItem buildItem(Long billId, int seq, FeeItem fi, String code, String name,
            String cls, String spec, String unit, BigDecimal price, BigDecimal qty,
            Long chargeDeptId, Long executeDeptId, Long orderId, Long orderItemId, String remark) {
        BillItem item = new BillItem();
        item.setBillId(billId);
        item.setItemSeq(seq);
        item.setFeeItemId(fi.getId());
        item.setItemCode(code != null ? code : fi.getItemCode());
        item.setItemName(name != null ? name : fi.getItemName());
        item.setItemClass(cls != null ? cls : fi.getItemClass());
        item.setSpec(spec != null ? spec : fi.getSpec());
        item.setUnit(unit != null ? unit : fi.getUnit());
        item.setUnitPrice(price != null ? price : fi.getUnitPrice());
        if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0
                || qty == null || qty.signum() <= 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        item.setQuantity(qty);
        item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
        if (item.getAmount().scale() > 4 || item.getAmount().precision() > 18) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        item.setChargeDeptId(chargeDeptId);
        item.setExecuteDeptId(executeDeptId);
        item.setOrderId(orderId);
        item.setOrderItemId(orderItemId);
        item.setChargeTime(LocalDateTime.now());
        item.setIsRefunded(0);
        item.setItemStatus("NORMAL");
        item.setRemark(remark);
        return item;
    }

    private void recalcBill(Bill bill) {
        List<BillItem> items = listItemsForBill(bill.getId());
        BigDecimal total = items.stream()
                .filter(i -> i.getIsRefunded() == null || i.getIsRefunded() == 0)
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setTotalAmount(total);
        BigDecimal disc = bill.getDiscountAmount() != null ? bill.getDiscountAmount() : BigDecimal.ZERO;
        bill.setPayableAmount(total.subtract(disc));
        billMapper.updateById(bill);
    }

    private List<BillItem> listItemsForBill(Long billId) {
        LambdaQueryWrapper<BillItem> q = new LambdaQueryWrapper<>();
        q.eq(BillItem::getBillId, billId).eq(BillItem::getDeleted, 0)
         .orderByAsc(BillItem::getItemSeq);
        return billItemMapper.selectList(q);
    }
}
