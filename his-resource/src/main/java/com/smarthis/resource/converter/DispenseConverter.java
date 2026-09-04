package com.smarthis.resource.converter;

import com.smarthis.resource.dto.request.DispenseCreateRequest;
import com.smarthis.resource.dto.response.DispenseItemVo;
import com.smarthis.resource.dto.response.DispenseVo;
import com.smarthis.resource.entity.Dispense;
import com.smarthis.resource.entity.DispenseItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class DispenseConverter {

    private DispenseConverter() {
    }

    public static Dispense toEntity(DispenseCreateRequest req) {
        Dispense e = new Dispense();
        e.setPatientId(req.getPatientId());
        e.setAdmissionId(req.getAdmissionId());
        e.setEncounterId(req.getEncounterId());
        e.setPharmacyId(req.getPharmacyId());
        e.setOrderId(req.getOrderId());
        e.setBillId(req.getBillId());
        e.setDispenseType(req.getDispenseType());
        e.setDispenseStatus("PENDING");
        e.setTotalAmount(req.getTotalAmount() != null ? req.getTotalAmount() : BigDecimal.ZERO);
        e.setRemark(req.getRemark());
        return e;
    }

    public static DispenseItem toItemEntity(Long dispenseId, DispenseCreateRequest.DispenseItemRequest req) {
        DispenseItem item = new DispenseItem();
        item.setDispenseId(dispenseId);
        item.setDrugId(req.getDrugId());
        item.setDrugCode(req.getDrugCode());
        item.setDrugName(req.getDrugName());
        item.setSpec(req.getSpec());
        item.setQuantity(req.getQuantity());
        item.setUnit(req.getUnit());
        item.setUnitPrice(req.getUnitPrice());
        if (req.getUnitPrice() != null && req.getQuantity() != null) {
            item.setAmount(req.getUnitPrice().multiply(req.getQuantity()));
        } else {
            item.setAmount(BigDecimal.ZERO);
        }
        item.setBatchNo(req.getBatchNo());
        item.setUsageMethod(req.getUsageMethod());
        item.setFrequency(req.getFrequency());
        item.setDays(req.getDays());
        item.setRemark(req.getRemark());
        return item;
    }

    public static DispenseVo toVo(Dispense e, List<DispenseItem> items) {
        DispenseVo vo = new DispenseVo();
        vo.setId(e.getId());
        vo.setDispenseNo(e.getDispenseNo());
        vo.setPatientId(e.getPatientId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setEncounterId(e.getEncounterId());
        vo.setPharmacyId(e.getPharmacyId());
        vo.setOrderId(e.getOrderId());
        vo.setBillId(e.getBillId());
        vo.setDispenseType(e.getDispenseType());
        vo.setDispenseStatus(e.getDispenseStatus());
        vo.setTotalAmount(e.getTotalAmount());
        vo.setDispenseTime(e.getDispenseTime());
        vo.setDispenserId(e.getDispenserId());
        vo.setDispenserName(e.getDispenserName());
        vo.setReviewerId(e.getReviewerId());
        vo.setReviewTime(e.getReviewTime());
        vo.setRemark(e.getRemark());
        vo.setCreatedTime(e.getCreatedTime());
        if (items != null) {
            vo.setItems(toItemVoList(items));
        } else {
            vo.setItems(new ArrayList<>());
        }
        return vo;
    }

    public static DispenseVo toVo(Dispense e) {
        return toVo(e, null);
    }

    public static DispenseItemVo toItemVo(DispenseItem item) {
        DispenseItemVo vo = new DispenseItemVo();
        vo.setId(item.getId());
        vo.setDispenseId(item.getDispenseId());
        vo.setDrugId(item.getDrugId());
        vo.setDrugCode(item.getDrugCode());
        vo.setDrugName(item.getDrugName());
        vo.setSpec(item.getSpec());
        vo.setQuantity(item.getQuantity());
        vo.setUnit(item.getUnit());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setAmount(item.getAmount());
        vo.setBatchNo(item.getBatchNo());
        vo.setUsageMethod(item.getUsageMethod());
        vo.setFrequency(item.getFrequency());
        vo.setDays(item.getDays());
        vo.setRemark(item.getRemark());
        return vo;
    }

    public static List<DispenseItemVo> toItemVoList(List<DispenseItem> items) {
        List<DispenseItemVo> voList = new ArrayList<>();
        for (DispenseItem item : items) {
            voList.add(toItemVo(item));
        }
        return voList;
    }
}
