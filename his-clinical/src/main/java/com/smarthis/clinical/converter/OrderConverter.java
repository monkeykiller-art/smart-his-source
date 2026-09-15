package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.OrderCreateRequest;
import com.smarthis.clinical.dto.request.OrderItemRequest;
import com.smarthis.clinical.dto.response.OrderItemVo;
import com.smarthis.clinical.dto.response.OrderTemplateItemVo;
import com.smarthis.clinical.dto.response.OrderTemplateVo;
import com.smarthis.clinical.dto.response.OrderVo;
import com.smarthis.clinical.entity.Order;
import com.smarthis.clinical.entity.OrderItem;
import com.smarthis.clinical.entity.OrderTemplate;
import com.smarthis.clinical.entity.OrderTemplateItem;

import java.util.List;
import java.math.RoundingMode;

public final class OrderConverter {

    private OrderConverter() {
    }

    public static Order toEntity(OrderCreateRequest req) {
        Order e = new Order();
        e.setPatientId(req.getPatientId());
        e.setEncounterId(req.getEncounterId());
        e.setAdmissionId(req.getAdmissionId());
        e.setDeptId(req.getDeptId());
        e.setDoctorId(req.getDoctorId());
        e.setOrderType(req.getOrderType());
        e.setOrderCategory(req.getOrderCategory() != null ? req.getOrderCategory() : "ROUTINE");
        e.setOrderStatus("DRAFT");
        e.setPriority(req.getPriority() != null ? req.getPriority() : 0);
        e.setIsStat(req.getIsStat() != null ? req.getIsStat() : 0);
        e.setIsPrn(req.getIsPrn() != null ? req.getIsPrn() : 0);
        e.setStartTime(req.getStartTime());
        e.setEndTime(req.getEndTime());
        e.setExecuteDeptId(req.getExecuteDeptId());
        e.setRemark(req.getRemark());
        e.setGroupNo(req.getGroupNo());
        return e;
    }

    public static OrderItem toItemEntity(Long orderId, int seq, OrderItemRequest req) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setItemSeq(seq);
        item.setItemCode(req.getItemCode());
        item.setItemName(req.getItemName());
        item.setItemType(req.getItemType() != null ? req.getItemType() : "DRUG");
        item.setSpec(req.getSpec());
        item.setDose(req.getDose());
        item.setDoseUnit(req.getDoseUnit());
        item.setUsageMethod(req.getUsageMethod());
        item.setFrequency(req.getFrequency());
        item.setDays(req.getDays());
        item.setQuantity(req.getQuantity() != null ? req.getQuantity() : java.math.BigDecimal.ONE);
        item.setQuantityUnit(req.getQuantityUnit());
        item.setUnitPrice(req.getUnitPrice());
        item.setIsFirstDay(req.getIsFirstDay() != null ? req.getIsFirstDay() : 0);
        item.setDripRate(req.getDripRate());
        item.setRemark(req.getRemark());
        item.setItemStatus("ACTIVE");
        if (req.getUnitPrice() != null && item.getQuantity() != null) {
            item.setAmount(req.getUnitPrice().multiply(item.getQuantity()).setScale(4, RoundingMode.HALF_UP));
        }
        return item;
    }

    public static OrderVo toVo(Order e) {
        OrderVo vo = new OrderVo();
        vo.setId(e.getId());
        vo.setOrderNo(e.getOrderNo());
        vo.setEncounterId(e.getEncounterId());
        vo.setAdmissionId(e.getAdmissionId());
        vo.setPatientId(e.getPatientId());
        vo.setDeptId(e.getDeptId());
        vo.setDoctorId(e.getDoctorId());
        vo.setOrderType(e.getOrderType());
        vo.setOrderCategory(e.getOrderCategory());
        vo.setOrderStatus(e.getOrderStatus());
        vo.setPriority(e.getPriority());
        vo.setIsStat(e.getIsStat());
        vo.setIsPrn(e.getIsPrn());
        vo.setStartTime(e.getStartTime());
        vo.setEndTime(e.getEndTime());
        vo.setOrderTime(e.getOrderTime());
        vo.setVerifyNurseId(e.getVerifyNurseId());
        vo.setVerifyTime(e.getVerifyTime());
        vo.setCancelNurseId(e.getCancelNurseId());
        vo.setCancelTime(e.getCancelTime());
        vo.setCancelReason(e.getCancelReason());
        vo.setExecuteDeptId(e.getExecuteDeptId());
        vo.setRemark(e.getRemark());
        vo.setGroupNo(e.getGroupNo());
        vo.setBillId(e.getBillId());
        vo.setCreatedTime(e.getCreatedTime());
        return vo;
    }

    public static OrderItemVo toItemVo(OrderItem item) {
        OrderItemVo vo = new OrderItemVo();
        vo.setId(item.getId());
        vo.setOrderId(item.getOrderId());
        vo.setItemSeq(item.getItemSeq());
        vo.setItemCode(item.getItemCode());
        vo.setItemName(item.getItemName());
        vo.setItemType(item.getItemType());
        vo.setSpec(item.getSpec());
        vo.setDose(item.getDose());
        vo.setDoseUnit(item.getDoseUnit());
        vo.setUsageMethod(item.getUsageMethod());
        vo.setFrequency(item.getFrequency());
        vo.setDays(item.getDays());
        vo.setQuantity(item.getQuantity());
        vo.setQuantityUnit(item.getQuantityUnit());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setAmount(item.getAmount());
        vo.setIsFirstDay(item.getIsFirstDay());
        vo.setDripRate(item.getDripRate());
        vo.setSkinTestResult(item.getSkinTestResult());
        vo.setRemark(item.getRemark());
        vo.setItemStatus(item.getItemStatus());
        return vo;
    }

    public static OrderTemplateVo toTemplateVo(OrderTemplate t) {
        OrderTemplateVo vo = new OrderTemplateVo();
        vo.setId(t.getId());
        vo.setTemplateName(t.getTemplateName());
        vo.setTemplateLevel(t.getTemplateLevel());
        vo.setDeptId(t.getDeptId());
        vo.setTemplateCategory(t.getTemplateCategory());
        vo.setLevelType(t.getLevelType());
        vo.setOrderType(t.getOrderType());
        vo.setSortOrder(t.getSortOrder());
        vo.setTemplateStatus(t.getTemplateStatus());
        return vo;
    }

    public static OrderTemplateItemVo toTemplateItemVo(OrderTemplateItem item) {
        OrderTemplateItemVo vo = new OrderTemplateItemVo();
        vo.setId(item.getId());
        vo.setTemplateId(item.getTemplateId());
        vo.setGroupNo(item.getGroupNo());
        vo.setItemCode(item.getItemCode());
        vo.setItemName(item.getItemName());
        vo.setSpec(item.getSpec());
        vo.setDose(item.getDose());
        vo.setDoseUnit(item.getDoseUnit());
        vo.setUsageMethod(item.getUsageMethod());
        vo.setFrequency(item.getFrequency());
        vo.setIsFirstDay(item.getIsFirstDay());
        vo.setQuantity(item.getQuantity());
        vo.setQuantityUnit(item.getQuantityUnit());
        vo.setDripRate(item.getDripRate());
        vo.setExecuteDeptId(item.getExecuteDeptId());
        vo.setDoctorAdvice(item.getDoctorAdvice());
        vo.setOrderCategory(item.getOrderCategory());
        vo.setItemSeq(item.getItemSeq());
        return vo;
    }
}
