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
import com.smarthis.operations.dto.response.BillVo;
import com.smarthis.operations.entity.Bill;
import com.smarthis.operations.entity.BillItem;
import com.smarthis.operations.entity.FeeItem;
import com.smarthis.operations.enums.BillStatus;
import com.smarthis.operations.enums.BillType;
import com.smarthis.operations.enums.VisitType;
import com.smarthis.operations.mapper.BillItemMapper;
import com.smarthis.operations.mapper.BillMapper;
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
        Bill bill = newBill(bizNoGenerator.next(BizNoType.BILL), request.getPatientId(),
                null, request.getEncounterId(), request.getVisitType(),
                request.getDeptId(), request.getBillType(), request.getRemark());
        billMapper.insert(bill);
        log.info("Bill from registration: billNo={}", bill.getBillNo());
        return BillConverter.toVo(bill);
    }

    @Override
    @Transactional
    public List<BillItemVo> addChargeItem(Long billId, BillChargeItemRequest request) {
        Bill bill = requireActiveBill(billId);
        FeeItem feeItem = requireFeeItem(request.getFeeItemId());
        int seq = nextSeq(billId);
        BillItem item = buildItem(billId, seq, feeItem,
                request.getItemCode(), request.getItemName(), request.getItemClass(),
                request.getSpec(), request.getUnit(), request.getUnitPrice(),
                request.getQuantity(), request.getChargeDeptId(), request.getExecuteDeptId(),
                request.getOrderId(), request.getOrderItemId(), request.getRemark());
        billItemMapper.insert(item);
        recalcBill(bill);
        log.info("Charge item added: billId={}, amount={}", billId, item.getAmount());
        return BillConverter.toItemVoList(listItems(billId));
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
        return BillConverter.toItemVoList(listItems(request.getBillId()));
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

    private Bill requireActiveBill(Long id) {
        Bill b = requireBill(id);
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
        item.setQuantity(qty);
        item.setAmount(item.getUnitPrice().multiply(item.getQuantity()));
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
        List<BillItem> items = listItems(bill.getId());
        BigDecimal total = items.stream()
                .filter(i -> i.getIsRefunded() == null || i.getIsRefunded() == 0)
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setTotalAmount(total);
        BigDecimal disc = bill.getDiscountAmount() != null ? bill.getDiscountAmount() : BigDecimal.ZERO;
        bill.setPayableAmount(total.subtract(disc));
        billMapper.updateById(bill);
    }

    private List<BillItem> listItems(Long billId) {
        LambdaQueryWrapper<BillItem> q = new LambdaQueryWrapper<>();
        q.eq(BillItem::getBillId, billId).eq(BillItem::getDeleted, 0)
         .orderByAsc(BillItem::getItemSeq);
        return billItemMapper.selectList(q);
    }
}
