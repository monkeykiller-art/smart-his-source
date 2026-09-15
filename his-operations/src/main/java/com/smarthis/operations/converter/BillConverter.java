package com.smarthis.operations.converter;

import com.smarthis.operations.dto.response.BillItemVo;
import com.smarthis.operations.dto.response.BillVo;
import com.smarthis.operations.entity.Bill;
import com.smarthis.operations.entity.BillItem;

import java.util.List;

public final class BillConverter {

    private BillConverter() {
    }

    public static BillVo toVo(Bill e) {
        BillVo vo = new BillVo();
        vo.setId(e.getId());
        vo.setBillNo(e.getBillNo());
        vo.setSourceType(e.getSourceType());
        vo.setSourceId(e.getSourceId());
        vo.setPatientId(e.getPatientId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setEncounterId(e.getEncounterId());
        vo.setVisitType(e.getVisitType() != null ? e.getVisitType().getValue() : null);
        vo.setDeptId(e.getDeptId());
        vo.setTotalAmount(e.getTotalAmount());
        vo.setDiscountAmount(e.getDiscountAmount());
        vo.setPayableAmount(e.getPayableAmount());
        vo.setPaidAmount(e.getPaidAmount());
        vo.setBillStatus(e.getBillStatus() != null ? e.getBillStatus().getValue() : null);
        vo.setBillType(e.getBillType() != null ? e.getBillType().getValue() : null);
        vo.setRemark(e.getRemark());
        vo.setVoidReason(e.getVoidReason());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }

    public static BillItemVo toItemVo(BillItem e) {
        BillItemVo vo = new BillItemVo();
        vo.setId(e.getId());
        vo.setBillId(e.getBillId());
        vo.setItemSeq(e.getItemSeq());
        vo.setFeeItemId(e.getFeeItemId());
        vo.setItemCode(e.getItemCode());
        vo.setItemName(e.getItemName());
        vo.setItemClass(e.getItemClass());
        vo.setSpec(e.getSpec());
        vo.setUnit(e.getUnit());
        vo.setUnitPrice(e.getUnitPrice());
        vo.setQuantity(e.getQuantity());
        vo.setAmount(e.getAmount());
        vo.setChargeDeptId(e.getChargeDeptId());
        vo.setExecuteDeptId(e.getExecuteDeptId());
        vo.setOrderId(e.getOrderId());
        vo.setOrderItemId(e.getOrderItemId());
        vo.setPrescTime(e.getPrescTime());
        vo.setChargeTime(e.getChargeTime());
        vo.setChargerId(e.getChargerId());
        vo.setIsRefunded(e.getIsRefunded());
        vo.setItemStatus(e.getItemStatus());
        vo.setRemark(e.getRemark());
        return vo;
    }

    public static List<BillItemVo> toItemVoList(List<BillItem> items) {
        return items.stream().map(BillConverter::toItemVo).toList();
    }
}
