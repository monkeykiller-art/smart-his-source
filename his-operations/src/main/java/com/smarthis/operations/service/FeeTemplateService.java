package com.smarthis.operations.service;

import com.smarthis.operations.dto.response.FeeTemplateItemVo;
import com.smarthis.operations.dto.response.FeeTemplateVo;

import java.math.BigDecimal;
import java.util.List;

public interface FeeTemplateService {
    FeeTemplateVo getById(Long id);
    List<FeeTemplateVo> listByDept(Long deptId);
    FeeTemplateVo create(String templateName, String templateCategory, String templateLevel, Long deptId);
    FeeTemplateVo update(Long id, String templateName, String templateCategory, Integer sortOrder);
    void delete(Long id);
    FeeTemplateItemVo addItem(Long templateId, String itemType, String itemCode, String itemName,
                              BigDecimal quantity, String unit, Long executeDeptId, Integer itemSeq);
    void removeItem(Long templateId, Long itemId);
}
