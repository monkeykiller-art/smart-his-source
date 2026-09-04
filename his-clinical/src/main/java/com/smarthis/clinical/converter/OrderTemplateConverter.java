package com.smarthis.clinical.converter;

import com.smarthis.clinical.dto.request.OrderTemplateCreateRequest;
import com.smarthis.clinical.dto.response.OrderTemplateItemVo;
import com.smarthis.clinical.dto.response.OrderTemplateVo;
import com.smarthis.clinical.entity.OrderTemplate;
import com.smarthis.clinical.entity.OrderTemplateItem;

public final class OrderTemplateConverter {

    private OrderTemplateConverter() {
    }

    public static OrderTemplate toEntity(OrderTemplateCreateRequest req) {
        OrderTemplate e = new OrderTemplate();
        e.setTemplateName(req.getTemplateName());
        e.setTemplateLevel(req.getTemplateLevel());
        e.setDeptId(req.getDeptId());
        e.setTemplateCategory(req.getTemplateCategory());
        e.setLevelType(req.getLevelType());
        e.setOrderType(req.getOrderType());
        e.setSortOrder(req.getSortOrder());
        e.setTemplateStatus(req.getTemplateStatus() != null ? req.getTemplateStatus() : 1);
        return e;
    }

    public static OrderTemplateVo toVo(OrderTemplate e) {
        OrderTemplateVo vo = new OrderTemplateVo();
        vo.setId(e.getId());
        vo.setTemplateName(e.getTemplateName());
        vo.setTemplateLevel(e.getTemplateLevel());
        vo.setDeptId(e.getDeptId());
        vo.setTemplateCategory(e.getTemplateCategory());
        vo.setLevelType(e.getLevelType());
        vo.setOrderType(e.getOrderType());
        vo.setSortOrder(e.getSortOrder());
        vo.setTemplateStatus(e.getTemplateStatus());
        return vo;
    }

    public static OrderTemplateItemVo toItemVo(OrderTemplateItem item) {
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
