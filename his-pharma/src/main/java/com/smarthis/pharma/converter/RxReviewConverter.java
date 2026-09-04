package com.smarthis.pharma.converter;

import com.smarthis.pharma.dto.request.RxReviewCreateRequest;
import com.smarthis.pharma.dto.request.RxReviewItemRequest;
import com.smarthis.pharma.dto.response.RxReviewItemVo;
import com.smarthis.pharma.dto.response.RxReviewVo;
import com.smarthis.pharma.entity.RxReview;
import com.smarthis.pharma.entity.RxReviewItem;

import java.util.List;

public final class RxReviewConverter {

    private RxReviewConverter() {
    }

    public static RxReview toEntity(RxReviewCreateRequest req) {
        RxReview e = new RxReview();
        e.setOrderId(req.getOrderId());
        e.setEncounterId(req.getEncounterId());
        e.setAdmissionId(req.getAdmissionId());
        e.setPatientId(req.getPatientId());
        e.setDoctorId(req.getDoctorId());
        e.setDeptId(req.getDeptId());
        e.setPrescriptionType(req.getPrescriptionType() != null ? req.getPrescriptionType() : "WESTERN");
        e.setReviewStatus("PENDING");
        e.setReviewResult(null);
        e.setWarningCount(0);
        e.setErrorCount(0);
        e.setRemark(req.getRemark());
        return e;
    }

    public static RxReviewItem toItemEntity(RxReviewItemRequest req, Long reviewId) {
        RxReviewItem item = new RxReviewItem();
        item.setReviewId(reviewId);
        item.setOrderItemId(req.getOrderItemId());
        item.setAlertType(req.getAlertType());
        item.setAlertLevel(req.getAlertLevel());
        item.setDrugCodeA(req.getDrugCodeA());
        item.setDrugNameA(req.getDrugNameA());
        item.setDrugCodeB(req.getDrugCodeB());
        item.setDrugNameB(req.getDrugNameB());
        item.setAlertDesc(req.getAlertDesc());
        item.setSuggestion(req.getSuggestion());
        item.setIsOverridden(0);
        return item;
    }

    public static RxReviewVo toVo(RxReview e) {
        RxReviewVo vo = new RxReviewVo();
        vo.setId(e.getId());
        vo.setReviewNo(e.getReviewNo());
        vo.setOrderId(e.getOrderId());
        vo.setEncounterId(e.getEncounterId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setPatientId(e.getPatientId());
        vo.setDoctorId(e.getDoctorId());
        vo.setDeptId(e.getDeptId());
        vo.setPrescriptionType(e.getPrescriptionType());
        vo.setReviewStatus(e.getReviewStatus());
        vo.setReviewResult(e.getReviewResult());
        vo.setReviewerId(e.getReviewerId());
        vo.setReviewerName(e.getReviewerName());
        vo.setReviewTime(e.getReviewTime());
        vo.setRejectReason(e.getRejectReason());
        vo.setWarningCount(e.getWarningCount());
        vo.setErrorCount(e.getErrorCount());
        vo.setRemark(e.getRemark());
        return vo;
    }

    public static RxReviewItemVo toItemVo(RxReviewItem e) {
        RxReviewItemVo vo = new RxReviewItemVo();
        vo.setId(e.getId());
        vo.setReviewId(e.getReviewId());
        vo.setOrderItemId(e.getOrderItemId());
        vo.setAlertType(e.getAlertType());
        vo.setAlertLevel(e.getAlertLevel());
        vo.setDrugCodeA(e.getDrugCodeA());
        vo.setDrugNameA(e.getDrugNameA());
        vo.setDrugCodeB(e.getDrugCodeB());
        vo.setDrugNameB(e.getDrugNameB());
        vo.setAlertDesc(e.getAlertDesc());
        vo.setSuggestion(e.getSuggestion());
        vo.setIsOverridden(e.getIsOverridden());
        vo.setOverrideReason(e.getOverrideReason());
        vo.setOverrideBy(e.getOverrideBy());
        return vo;
    }

    public static List<RxReviewItemVo> toItemVoList(List<RxReviewItem> items) {
        return items.stream().map(RxReviewConverter::toItemVo).toList();
    }
}
