package com.smarthis.operations.converter;

import com.smarthis.operations.dto.request.FeeTemplateCreateRequest;
import com.smarthis.operations.dto.request.FeeTemplateItemRequest;
import com.smarthis.operations.dto.response.FeeTemplateItemVo;
import com.smarthis.operations.dto.response.FeeTemplateVo;
import com.smarthis.operations.entity.FeeTemplate;
import com.smarthis.operations.entity.FeeTemplateItem;

import java.util.List;

public final class FeeTemplateConverter {

    private FeeTemplateConverter() {
    }

    public static FeeTemplate toEntity(FeeTemplateCreateRequest req) {
        FeeTemplate e = new FeeTemplate();
        e.setTemplateName(req.getTemplateName());
        e.setTemplateCategory(req.getTemplateCategory());
        e.setTemplateLevel(req.getTemplateLevel());
        e.setDeptId(req.getDeptId());
        e.setLevelType(req.getLevelType());
        e.setSortOrder(req.getSortOrder());
        e.setTemplateStatus(1);
        return e;
    }

    public static FeeTemplateItem toItemEntity(FeeTemplateItemRequest req, Long templateId) {
        FeeTemplateItem e = new FeeTemplateItem();
        e.setTemplateId(templateId);
        e.setItemType(req.getItemType());
        e.setItemCode(req.getItemCode());
        e.setItemName(req.getItemName());
        e.setQuantity(req.getQuantity());
        e.setUnit(req.getUnit());
        e.setExecuteDeptId(req.getExecuteDeptId());
        e.setItemSeq(req.getItemSeq());
        return e;
    }

    public static FeeTemplateVo toVo(FeeTemplate e) {
        FeeTemplateVo vo = new FeeTemplateVo();
        vo.setId(e.getId());
        vo.setTemplateName(e.getTemplateName());
        vo.setTemplateCategory(e.getTemplateCategory());
        vo.setTemplateLevel(e.getTemplateLevel());
        vo.setDeptId(e.getDeptId());
        vo.setLevelType(e.getLevelType());
        vo.setSortOrder(e.getSortOrder());
        vo.setTemplateStatus(e.getTemplateStatus());
        return vo;
    }

    public static FeeTemplateItemVo toItemVo(FeeTemplateItem e) {
        FeeTemplateItemVo vo = new FeeTemplateItemVo();
        vo.setId(e.getId());
        vo.setTemplateId(e.getTemplateId());
        vo.setItemType(e.getItemType());
        vo.setItemCode(e.getItemCode());
        vo.setItemName(e.getItemName());
        vo.setQuantity(e.getQuantity());
        vo.setUnit(e.getUnit());
        vo.setExecuteDeptId(e.getExecuteDeptId());
        vo.setItemSeq(e.getItemSeq());
        return vo;
    }

    public static List<FeeTemplateItemVo> toItemVoList(List<FeeTemplateItem> items) {
        return items.stream().map(FeeTemplateConverter::toItemVo).toList();
    }
}
