package com.smarthis.operations.converter;

import com.smarthis.operations.dto.response.SettlementItemVo;
import com.smarthis.operations.dto.response.SettlementPreviewVo;
import com.smarthis.operations.dto.response.SettlementVo;
import com.smarthis.operations.entity.Settlement;
import com.smarthis.operations.entity.SettlementItem;

import java.util.List;

public final class SettlementConverter {

    private SettlementConverter() {
    }

    public static SettlementVo toVo(Settlement e) {
        SettlementVo vo = new SettlementVo();
        vo.setId(e.getId());
        vo.setSettleNo(e.getSettleNo());
        vo.setPatientId(e.getPatientId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setEncounterId(e.getEncounterId());
        vo.setVisitType(e.getVisitType() != null ? e.getVisitType().getValue() : null);
        vo.setPatientType(e.getPatientType());
        vo.setInvoiceNo(e.getInvoiceNo());
        vo.setTotalAmount(e.getTotalAmount());
        vo.setInsuranceAmount(e.getInsuranceAmount());
        vo.setDepositAmount(e.getDepositAmount());
        vo.setSelfPayAmount(e.getSelfPayAmount());
        vo.setSettleBalance(e.getSettleBalance());
        vo.setSettleType(e.getSettleType() != null ? e.getSettleType().getValue() : null);
        vo.setPayMethod(e.getPayMethod() != null ? e.getPayMethod().getValue() : null);
        vo.setCashierId(e.getCashierId());
        vo.setCashierName(e.getCashierName());
        vo.setSettleTime(e.getSettleTime());
        vo.setSettleStatus(e.getSettleStatus());
        vo.setRemark(e.getRemark());
        return vo;
    }

    public static SettlementItemVo toItemVo(SettlementItem e) {
        SettlementItemVo vo = new SettlementItemVo();
        vo.setId(e.getId());
        vo.setSettlementId(e.getSettlementId());
        vo.setItemClass(e.getItemClass());
        vo.setItemCount(e.getItemCount());
        vo.setTotalAmount(e.getTotalAmount());
        vo.setInsuranceAmount(e.getInsuranceAmount());
        vo.setSelfPayAmount(e.getSelfPayAmount());
        return vo;
    }

    public static List<SettlementItemVo> toItemVoList(List<SettlementItem> items) {
        return items.stream().map(SettlementConverter::toItemVo).toList();
    }

    public static SettlementPreviewVo toPreviewVo(Long patientId, Long admissionId) {
        SettlementPreviewVo vo = new SettlementPreviewVo();
        vo.setPatientId(patientId);
        vo.setAdmissionId(admissionId);
        return vo;
    }
}
