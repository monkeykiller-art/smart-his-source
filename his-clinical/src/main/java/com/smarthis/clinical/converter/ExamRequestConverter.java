package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.ExamRequestCreateRequest;
import com.smarthis.clinical.dto.request.ExamRequestItemRequest;
import com.smarthis.clinical.dto.response.ExamRequestItemVo;
import com.smarthis.clinical.dto.response.ExamRequestVo;
import com.smarthis.clinical.entity.ExamRequest;
import com.smarthis.clinical.entity.ExamRequestItem;

public final class ExamRequestConverter {

    private ExamRequestConverter() {
    }

    public static ExamRequest toEntity(ExamRequestCreateRequest req) {
        ExamRequest e = new ExamRequest();
        e.setPatientId(req.getPatientId());
        e.setEncounterId(req.getEncounterId());
        e.setAdmissionId(req.getAdmissionId());
        e.setDeptId(req.getDeptId());
        e.setDoctorId(req.getDoctorId());
        e.setRequestType(req.getRequestType());
        e.setIsUrgent(req.getIsUrgent() != null ? req.getIsUrgent() : 0);
        e.setClinicalDiagnosis(req.getClinicalDiagnosis());
        e.setClinicalInfo(req.getClinicalInfo());
        e.setRequestDeptId(req.getRequestDeptId());
        e.setExecuteDeptId(req.getExecuteDeptId());
        e.setRequestStatus("SUBMITTED");
        e.setRequestTime(java.time.LocalDateTime.now());
        e.setRemark(req.getRemark());
        return e;
    }

    public static ExamRequestItem toItemEntity(Long requestId, int seq, ExamRequestItemRequest req) {
        ExamRequestItem item = new ExamRequestItem();
        item.setRequestId(requestId);
        item.setItemSeq(seq);
        item.setItemCode(req.getItemCode());
        item.setItemName(req.getItemName());
        item.setItemType(req.getItemType());
        item.setSpec(req.getSpec());
        item.setQuantity(req.getQuantity() != null ? req.getQuantity() : java.math.BigDecimal.ONE);
        item.setUnitPrice(req.getUnitPrice());
        if (req.getUnitPrice() != null && item.getQuantity() != null) {
            item.setAmount(req.getUnitPrice().multiply(item.getQuantity()));
        }
        item.setBodyPart(req.getBodyPart());
        item.setMethodDesc(req.getMethodDesc());
        item.setItemStatus("ACTIVE");
        item.setConfirmStatus("UNCONFIRMED");
        return item;
    }

    public static ExamRequestVo toVo(ExamRequest e) {
        ExamRequestVo vo = new ExamRequestVo();
        vo.setId(e.getId());
        vo.setRequestNo(e.getRequestNo());
        vo.setEncounterId(e.getEncounterId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setPatientId(e.getPatientId());
        vo.setDeptId(e.getDeptId());
        vo.setDoctorId(e.getDoctorId());
        vo.setRequestType(e.getRequestType());
        vo.setIsUrgent(e.getIsUrgent());
        vo.setClinicalDiagnosis(e.getClinicalDiagnosis());
        vo.setClinicalInfo(e.getClinicalInfo());
        vo.setRequestDeptId(e.getRequestDeptId());
        vo.setExecuteDeptId(e.getExecuteDeptId());
        vo.setRequestStatus(e.getRequestStatus());
        vo.setRequestTime(e.getRequestTime());
        vo.setResultTime(e.getResultTime());
        vo.setResultSummary(e.getResultSummary());
        vo.setReportNo(e.getReportNo());
        vo.setIsCritical(e.getIsCritical());
        vo.setCriticalAcknowledged(e.getCriticalAcknowledged());
        vo.setCriticalAckBy(e.getCriticalAckBy());
        vo.setCriticalAckTime(e.getCriticalAckTime());
        vo.setReportUrl(e.getReportUrl());
        vo.setIsPrinted(e.getIsPrinted());
        vo.setPrintTime(e.getPrintTime());
        vo.setRemark(e.getRemark());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }

    public static ExamRequestItemVo toItemVo(ExamRequestItem item) {
        ExamRequestItemVo vo = new ExamRequestItemVo();
        vo.setId(item.getId());
        vo.setRequestId(item.getRequestId());
        vo.setItemSeq(item.getItemSeq());
        vo.setItemCode(item.getItemCode());
        vo.setItemName(item.getItemName());
        vo.setItemType(item.getItemType());
        vo.setSpec(item.getSpec());
        vo.setQuantity(item.getQuantity());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setAmount(item.getAmount());
        vo.setBodyPart(item.getBodyPart());
        vo.setMethodDesc(item.getMethodDesc());
        vo.setItemStatus(item.getItemStatus());
        vo.setConfirmStatus(item.getConfirmStatus());
        vo.setConfirmTime(item.getConfirmTime());
        return vo;
    }
}
